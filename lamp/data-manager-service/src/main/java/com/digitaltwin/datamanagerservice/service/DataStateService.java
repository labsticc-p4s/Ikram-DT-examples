package com.digitaltwin.datamanagerservice.service;

import com.digitaltwin.datamanagerservice.kafka.DataStateProducer;
import com.digitaltwin.datamanagerservice.model.DataStateModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j

public class DataStateService {

    private final DataStateProducer producer;
    private final RestTemplate restTemplate;

    private final ConcurrentHashMap<String, Map> configCache = new ConcurrentHashMap<>();

    private static final String ENV_URL = "http://env-service:8090";




    public void enrich(Map<String, Object> raw, String source) {
        String  lampId      = (String)  raw.get("lampId");
        boolean isOn        = Boolean.TRUE.equals(raw.get("isOn"));
        double  brightness  = toDouble(raw.get("brightness"));
        double  usageMin    = toDouble(raw.get("usageMinutes"));
        double  rawTemp     = toDouble(raw.get("temperature"));
        double  rawRoomTemp = toDouble(raw.get("roomTemp"));

        if ("SIMULATION".equals(source)) {
            producer.send(DataStateModel.builder()
                    .lampId(lampId)
                    .source(source)
                    .isOn(isOn)
                    .brightness(brightness)
                    .usageMinutes(usageMin)
                    .roomTemp(rawRoomTemp)
                    .temperature(rawTemp)
                    .hasTemperatureSensor(true)
                    .maxWatts(60.0)
                    .maxLifeMinutes(1_500_000.0)
                    .build());
            return;
        }

        Map config = fetchConfig(lampId);

        boolean hasRoomTempSensor    = !Boolean.FALSE.equals(config.get("hasRoomTempSensor"));
        boolean hasTemperatureSensor = !Boolean.FALSE.equals(config.get("hasTemperatureSensor"));
        double  maxWatts             = config.containsKey("maxWatts")       ? toDouble(config.get("maxWatts"))       : 60.0;
        double  maxLifeMinutes       = config.containsKey("maxLifeMinutes") ? toDouble(config.get("maxLifeMinutes")) : 1_500_000.0;
        String  room                 = config.containsKey("room")           ? (String) config.get("room")            : "default";

        double roomTemp;
        if (hasRoomTempSensor && rawRoomTemp >= 0) {
            roomTemp = rawRoomTemp;
        } else {
            roomTemp = fetchRoomTemp(room);
        }

        double temperature = hasTemperatureSensor ? rawTemp : -1.0;

        producer.send(DataStateModel.builder()
                .lampId(lampId)
                .source(source)
                .isOn(isOn)
                .brightness(brightness)
                .usageMinutes(usageMin)
                .roomTemp(roomTemp)
                .temperature(temperature)
                .hasTemperatureSensor(hasTemperatureSensor)
                .maxWatts(maxWatts)
                .maxLifeMinutes(maxLifeMinutes)
                .build());
    }

    private double fetchRoomTemp(String roomId) {
        try {
            Map resp = restTemplate.getForObject(
                    ENV_URL + "/api/env/room/" + roomId + "/state", Map.class);
            if (resp != null && resp.get("roomTemp") != null)
                return toDouble(resp.get("roomTemp"));
        } catch (Exception e) {
            log.debug("Cannot fetch room temp for room {} — using default", roomId);
        }
        return 22.0;
    }

    private Map fetchConfig(String lampId) {
        return configCache.computeIfAbsent(lampId, id -> {
            try {
                int    n    = Integer.parseInt(id.replaceAll("\\D", ""));
                int    port = (n == 1) ? 8081 : (n == 2) ? 8091 : 8090 + n;
                String url  = "http://physical-service-" + n + ":" + port
                        + "/api/physical/registry/" + id;
                Map cfg = restTemplate.getForObject(url, Map.class);
                return cfg != null ? cfg : Map.of();
            } catch (Exception e) {
                log.debug("No registry entry for {} — using defaults", id);
                configCache.remove(lampId);
                return Map.of();
            }
        });
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        return ((Number) v).doubleValue();
    }
}

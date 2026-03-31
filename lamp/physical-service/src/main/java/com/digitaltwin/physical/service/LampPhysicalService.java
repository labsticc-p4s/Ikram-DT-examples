package com.digitaltwin.physical.service;
import com.digitaltwin.physical.kafka.PhysicalLampProducer;
import com.digitaltwin.physical.model.LampState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LampPhysicalService {

    private final PhysicalLampProducer producer;
    private final RestTemplate restTemplate;

    @Value("${lamp.id}") private String lampId;
    @Value("${lamp.room}") private String roomId;

    @Value("${lamp.hasRoomTempSensor:true}")    private boolean hasRoomTempSensor;
    @Value("${lamp.hasTemperatureSensor:true}") private boolean hasTemperatureSensor;

    private static final String ENV_URL = "http://env-service:8090";


    private boolean isOn = false;
    private double brightness = 0;
    private double roomTemp = 22.0;
    private double usageMinutes = 0.0;
    private double currentTemp = 22.0;


    private double maxTmemp() {
        return isOn ? roomTemp + 20 + (brightness * 0.25) : roomTemp;
    }

    private void updateTemp() {
        double t = maxTmemp();
        double step = isOn ? 2.0 : 0.5; //increment the temp with 2C if it is on else with 0,5
        if (currentTemp < t) currentTemp = Math.min(currentTemp + step, t); //heating but never surpass the max temp
        else if (currentTemp > t) currentTemp = Math.max(currentTemp - step, t); //cooling down but to reach room temp
    }

    private void getRoomTemp() {
        if (!hasRoomTempSensor) return; //only if lamp has no room temp
        try {
            Map resp = restTemplate.getForObject(
                    ENV_URL + "/api/env/room/" + roomId + "/state", Map.class);
            if (resp != null && resp.get("roomTemp") != null)
                roomTemp = ((Number) resp.get("roomTemp")).doubleValue();
        } catch (Exception e) {
            log.debug("Could not fetch room temp for room {} — keeping last value", roomId);
        }
    }

    //send state of physical lamp each 1 second alogside with the roomtemp the lamps belongs to
    @Scheduled(fixedRate = 1000)
    public void update() {
        getRoomTemp();
        updateTemp();
        if (isOn) usageMinutes += 1.0 / 60.0;
        producer.send(buildState());
    }

    public LampState buildState() {
        return LampState.builder()
                .lampId(lampId)
                .isOn(isOn)
                .brightness(brightness)
                // if no temperature sensor → publish -1 as signal to model-service
                .temperature(hasTemperatureSensor
                        ? Math.round(currentTemp * 10) / 10.0
                        : -1.0)
                .usageMinutes(Math.round(usageMinutes * 100) / 100.0)
                // if no room temp sensor → publish -1 as signal to model-service
                .roomTemp(hasRoomTempSensor ? roomTemp : -1.0)
                .build();
    }

    public LampState getState() {
        return LampState.builder()
                .lampId(lampId)
                .isOn(isOn)
                .brightness(brightness)
                .temperature(hasTemperatureSensor ? Math.round(currentTemp * 10) / 10.0 : -1.0)
                .usageMinutes(Math.round(usageMinutes * 100) / 100.0)
                .roomTemp(roomTemp)
                .build();
    }

    public LampState turnOn(double b){
        isOn=true;
        brightness=Math.max(0,Math.min(100,b));
        return getState();
    }

    public LampState turnOff(){
        isOn=false;
        brightness=0;
        return getState();
    }

    public LampState setBrightness(double b) {
        brightness=Math.max(0,Math.min(100,b));
        isOn=brightness>0;
        return getState();
    }

}

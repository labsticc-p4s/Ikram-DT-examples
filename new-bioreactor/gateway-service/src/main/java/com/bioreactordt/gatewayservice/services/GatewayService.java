package com.bioreactordt.gatewayservice.services;

import com.bioreactordt.gatewayservice.models.Experimentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    private final ConcurrentHashMap<String, String> activeExperiments = new ConcurrentHashMap<>();

    public String startTwin(Experimentation request) throws Exception {
        String experimentId = UUID.randomUUID().toString();

        Experimentation experiment = Experimentation.builder()
                .experimentId(experimentId)
                .reactorId(request.getReactorId())
                .condInit(request.getCondInit())
                .populationModel(request.getPopulationModel())
                .phModel(request.getPhModel())
                .tempModel(request.getTempModel())
                .source("physical")
                .build();

        kafkaTemplate.send("experiment-created", objectMapper.writeValueAsString(experiment));

        activeExperiments.put(request.getReactorId(), experimentId);

        log.info("twin started reactor={} expId={}", request.getReactorId(), experimentId);
        return experimentId;
    }



    public void stopTwin(String reactorId) {
        String removed = activeExperiments.remove(reactorId);
        log.info("twin stopped reactor={} expId={}", reactorId, removed);
    }

    public String getActivePhysicalExp(String reactorId) {
        return activeExperiments.get(reactorId);
    }
}

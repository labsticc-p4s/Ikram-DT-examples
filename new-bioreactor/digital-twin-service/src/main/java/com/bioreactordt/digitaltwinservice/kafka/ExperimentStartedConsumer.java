package com.bioreactordt.digitaltwinservice.kafka;

import com.bioreactordt.digitaltwinservice.services.SimulationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;



@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentStartedConsumer {

    private final ObjectMapper objectMapper;
    private final SimulationService simulationService;

    @KafkaListener(topics = "experiment-started", groupId = "digital-twin-service")
    public void onExperimentStarted(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);
            String experimentId = (String) payload.get("experimentId");
            String source = (String) payload.get("source");
            if (!"sim".equals(source)) return;
            simulationService.runPendingSimulation(experimentId);

        } catch (Exception e) {
            log.error("Failed to process experiment-started: {}", e.getMessage());
        }
    }
}
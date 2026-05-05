package com.bioreactordt.modelsservice.kafka;

import com.bioreactordt.modelsservice.models.Experimentation;
import com.bioreactordt.modelsservice.services.ModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentConsumer {

    private final ObjectMapper objectMapper;
    private final ModelService service;

    @KafkaListener(topics = "experiment-started", groupId = "models-service")
    public void onExperiment(String json) {
        try {
            Experimentation exp = objectMapper.readValue(json, Experimentation.class);
            service.onExperimentStarted(exp);

        } catch (Exception e) {
            log.error("Failed to process bioreactor-state: {}", e.getMessage());
        }
    }


}

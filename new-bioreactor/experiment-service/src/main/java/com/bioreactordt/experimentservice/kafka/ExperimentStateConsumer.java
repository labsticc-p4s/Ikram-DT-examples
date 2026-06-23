package com.bioreactordt.experimentservice.kafka;

import com.bioreactordt.experimentservice.models.Experimentation;
import com.bioreactordt.experimentservice.models.ExperimentationState;
import com.bioreactordt.experimentservice.services.ExperimentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentStateConsumer {

    private final ObjectMapper objectMapper;
    private final ExperimentService service;




    @KafkaListener(topics = "experiment-created", groupId = "experiment-service")
    public void onExperimentCreated(String json) {
        try {
            Experimentation ex = objectMapper.readValue(json, Experimentation.class);
            service.createExperiment(ex);
        } catch (Exception e) {
            log.error("Failed to process experiment-created: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "experiment-state", groupId = "experiment-service")
    public void onExperimentState(String json) {
        try {
            ExperimentationState state = objectMapper.readValue(json, ExperimentationState.class);
            service.createExperimentState(state);
        } catch (Exception e) {
            log.error("Failed to process experiment-state: {}", e.getMessage());
        }
    }






}

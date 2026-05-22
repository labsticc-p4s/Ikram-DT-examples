package com.bioreactordt.modelsservice.kafka;


import com.bioreactordt.modelsservice.models.ExperimentationState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExperimentStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String experimentId, ExperimentationState state) {
        try {
            kafkaTemplate.send("experiment-state", experimentId, objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            log.error("Failed to send experimentation state: {}", e.getMessage());
        }
    }
}

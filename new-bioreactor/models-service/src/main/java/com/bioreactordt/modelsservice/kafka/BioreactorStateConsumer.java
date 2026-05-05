package com.bioreactordt.modelsservice.kafka;


import com.bioreactordt.modelsservice.models.BioreactorState;
import com.bioreactordt.modelsservice.services.ModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BioreactorStateConsumer {

    private final ObjectMapper objectMapper;
    private final ModelService service;

    @KafkaListener(topics = "bioreactor-state-normalized", groupId = "models-service")
    public void onBioreactorState(String json) {
        try {
            BioreactorState state = objectMapper.readValue(json, BioreactorState.class);
            service.onBioreactorState(state);

        } catch (Exception e) {
            log.error("Failed to process bioreactor-state: {}", e.getMessage());
        }
    }

}

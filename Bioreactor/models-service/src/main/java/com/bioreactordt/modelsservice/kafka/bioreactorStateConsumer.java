package com.bioreactordt.modelsservice.kafka;

import com.bioreactordt.modelsservice.models.BioreactorState;
import com.bioreactordt.modelsservice.services.BioreactorModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class BioreactorStateConsumer {

    private final BioreactorModelService computeService;
    private final ObjectMapper mapper;



    @KafkaListener(topics = "twin-simulation", groupId = "model-simulation-group")
    public void onSimulation(String msg) {
        try {
            BioreactorState state = mapper.readValue(msg, BioreactorState.class);
            if (state.getSource() == null) state.setSource("SIMULATION");
            computeService.compute(state);
        } catch (Exception e) {
            log.error("Failed to process twin-simulation: {}", e.getMessage());
        }
    }
}
package com.bioreactordt.modelsservice.kafka;

import com.bioreactordt.modelsservice.models.bioreactorState;
import com.bioreactordt.modelsservice.services.bioreactorModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class bioreactorStateConsumer {

    private final ObjectMapper mapper;
    private final bioreactorModelService serivce;


   //consume bioreactor state
    @KafkaListener(topics = "bioreactor-state", groupId = "model-physical-group")
    public void onPhysical(String msg) {
        try {
            serivce.compute(mapper.readValue(msg, bioreactorState.class), "PHYSICAL");
        } catch (Exception e) {
            log.error("Failed to process physical state: {}", e.getMessage());
        }
    }

    //consume sim state
    @KafkaListener(topics = "twin-simulation", groupId = "model-simulation-group")
    public void onSimulation(String msg) {
        try {
            serivce.compute(mapper.readValue(msg, bioreactorState.class), "SIMULATION");
        } catch (Exception e) {
            log.error("Failed to process simulation state: {}", e.getMessage());
        }
    }

}

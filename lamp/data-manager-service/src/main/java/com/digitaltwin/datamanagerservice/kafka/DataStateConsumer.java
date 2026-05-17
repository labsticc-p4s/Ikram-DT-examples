package com.digitaltwin.datamanagerservice.kafka;

import com.digitaltwin.datamanagerservice.service.DataStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j

public class DataStateConsumer {

    private final DataStateService service;
    private final ObjectMapper mapper;

    // receives raw physical lamp states
    @KafkaListener(topics = "lamp-state", groupId = "data-manager-physical-group")
    public void onPhysical(String msg) {
        try {
            service.enrich(mapper.readValue(msg, Map.class), "PHYSICAL");
        } catch (Exception e) {
            log.error("Failed to process physical state: {}", e.getMessage());
        }
    }

    // receives simulation states from digital-twin-service
    @KafkaListener(topics = "twin-simulation", groupId = "data-manager-simulation-group")
    public void onSimulation(String msg) {
        try {
            service.enrich(mapper.readValue(msg, Map.class), "SIMULATION");
        } catch (Exception e) {
            log.error("Failed to process simulation state: {}", e.getMessage());
        }
    }
}


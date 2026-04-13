package com.bioreactordt.gatewayservice.kafka;
import com.bioreactordt.gatewayservice.models.*;
import com.bioreactordt.gatewayservice.services.EnrichedStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BioreactorStateConsumer {

    private final EnrichedStateService  service;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper  mapper;

    @KafkaListener(topics = "bioreactor-state", groupId = "gateway-group")
    public void bioreactorRawData(String message) {
        try {
            BioreactorState raw = mapper.readValue(message, BioreactorState.class);
            EnrichedBioreactorState enriched = service.mergeData(raw);

            if (!enriched.isValid()) {
                log.warn(enriched.getInvalidReason());
                return;
            }

            kafkaTemplate.send("enriched-state", enriched.getReactorId(), mapper.writeValueAsString(enriched));

        } catch (Exception e) {
            log.error("Gateway failed to read data : {}", e.getMessage());
        }
    }
}

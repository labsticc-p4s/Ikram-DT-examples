package com.bioreactordt.gatewayservice.kafka;


import com.bioreactordt.gatewayservice.models.BioreactorState;
import com.bioreactordt.gatewayservice.services.GatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BioreactorConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final GatewayService service;


    @KafkaListener(topics = "bioreactor-state", groupId = "models-service")
    public void onBioreactorState(String json) {
        try {
            BioreactorState raw = objectMapper.readValue(json, BioreactorState.class);
            String expId = service.getActivePhysicalExp(raw.getReactorId());
            if (expId == null) {
                log.debug("No active experiment found for reactor {}", raw.getReactorId());
                return;
            }
            BioreactorState state = BioreactorState.builder()
                    .reactorId(raw.getReactorId())
                    .experimentId(expId)
                    .sensorReadings(raw.getSensorReadings())
                    .build();

            kafkaTemplate.send("bioreactor-state-normalized", state.getReactorId(), objectMapper.writeValueAsString(state));

        } catch (Exception e) {
            log.error("Failed to process bioreactor-state: {}", e.getMessage());
        }
    }
}

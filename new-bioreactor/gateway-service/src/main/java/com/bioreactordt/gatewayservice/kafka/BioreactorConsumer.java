package com.bioreactordt.gatewayservice.kafka;


import com.bioreactordt.gatewayservice.models.BioreactorState;
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


    @KafkaListener(topics = "bioreactor-state", groupId = "models-service")
    public void onBioreactorState(String json) {
        try {
            BioreactorState raw = objectMapper.readValue(json, BioreactorState.class);

            BioreactorState state = BioreactorState.builder()
                    .reactorId(raw.getReactorId())
                    .sensorReadings(raw.getSensorReadings())
                    .build();
            kafkaTemplate.send("bioreactor-state-normalized", state.getReactorId(), objectMapper.writeValueAsString(state));
            log.info("Forwarded bioreactor state for reactor {}", state.getReactorId());

        } catch (Exception e) {
            log.error("Failed to process bioreactor-state: {}", e.getMessage());
        }
    }
}

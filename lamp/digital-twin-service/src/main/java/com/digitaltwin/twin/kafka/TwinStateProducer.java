package com.digitaltwin.twin.kafka;
import com.digitaltwin.twin.model.LampState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class TwinStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendSimulation(String simId, LampState state) {
        try {
            kafkaTemplate.send("twin-simulation", state.getLampId(), objectMapper.writeValueAsString(state));
        }
        catch (Exception e) {
            log.error("Failed to send sim state simId={}", simId, e);
        }
    }
}

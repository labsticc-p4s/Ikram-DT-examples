package com.bioreactordt.digitaltwinservice.kafka;

import com.bioreactordt.digitaltwinservice.models.bioreactorModelResult;
import com.bioreactordt.digitaltwinservice.models.bioreactorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
@RequiredArgsConstructor
@Slf4j
public class twinStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String simId, bioreactorState state ) {
        try {
            kafkaTemplate.send("twin-simulation", state.getReactorId(), objectMapper.writeValueAsString(state));
        }
        catch (Exception e) {
            log.error("Failed to send sim state simId={}", simId);
        }
    }

}

package com.bioreactordt.bioreactormockservice.kafka;

import com.bioreactordt.bioreactormockservice.models.bioreactorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class bioreactorProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(bioreactorState state) {
        try {
            kafkaTemplate.send("bioreactor-state", state.getReactorId(), objectMapper.writeValueAsString(state));
        }
        catch (Exception e) {
            log.error("Failed to send bioreactor state", e);
        }
    }
}

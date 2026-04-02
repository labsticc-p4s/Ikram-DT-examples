package com.bioreactordt.modelsservice.kafka;

import com.bioreactordt.modelsservice.models.bioreactorModelResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
@RequiredArgsConstructor
@Slf4j
public class bioreactorModelResultProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(bioreactorModelResult result) {
        try {
            kafkaTemplate.send("model-results", result.getReactorId(), objectMapper.writeValueAsString(result));
        }
        catch (Exception e) {
            log.error("Failed to send model result", e);
        }
    }
}

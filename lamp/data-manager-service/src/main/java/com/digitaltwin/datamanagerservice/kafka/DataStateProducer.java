package com.digitaltwin.datamanagerservice.kafka;

import com.digitaltwin.datamanagerservice.model.DataStateModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j

public class DataStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(DataStateModel state) {
        try {
            kafkaTemplate.send("enriched-states", state.getLampId(),
                    objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            log.error("Failed to send enriched state: {}", e.getMessage());
        }
    }
}

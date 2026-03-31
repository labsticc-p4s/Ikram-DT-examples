package com.digitaltwin.models.kafka;
import com.digitaltwin.models.model.ModelResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @Slf4j
public class ModelProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(ModelResult r) {
        try {
            kafkaTemplate.send("model-results", r.getLampId(), objectMapper.writeValueAsString(r));
        }
        catch (Exception e) {
            log.error("Failed to send model result", e);
        }
    }
}

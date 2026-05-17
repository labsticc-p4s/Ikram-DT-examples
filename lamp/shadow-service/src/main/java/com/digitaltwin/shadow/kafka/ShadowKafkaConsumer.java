package com.digitaltwin.shadow.kafka;
import com.digitaltwin.shadow.model.ModelResult;
import com.digitaltwin.shadow.service.ShadowStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class ShadowKafkaConsumer {

    private final ShadowStorageService storageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "model-results", groupId = "shadow-model-group")
    public void onModelResult(String message) {
        try {
            storageService.store(objectMapper.readValue(message, ModelResult.class));
        }
        catch (Exception e) {
            log.error("storage failed: {}", e.getMessage());
        }
    }
}

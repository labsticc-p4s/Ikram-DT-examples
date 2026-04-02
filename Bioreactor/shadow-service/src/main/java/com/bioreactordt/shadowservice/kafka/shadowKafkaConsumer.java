package com.bioreactordt.shadowservice.kafka;

import com.bioreactordt.shadowservice.models.bioreactorModelResult;
import com.bioreactordt.shadowservice.services.shadowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
@Slf4j
public class shadowKafkaConsumer {

    private final shadowService storageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "model-results", groupId = "shadow-model-group")
    public void onModelResult(String message) {
        try {
            storageService.store(objectMapper.readValue(message, bioreactorModelResult.class));
        } catch (Exception e) {
            log.error("Shadow storage failed: {}", e.getMessage());
        }
    }
}

package com.digitaltwin.twin.kafka;
import com.digitaltwin.twin.model.ModelResult;
import com.digitaltwin.twin.service.SimulationService;
import com.digitaltwin.twin.service.SyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelResultConsumer {

    private final SyncService syncService;
    private final SimulationService simService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "model-results", groupId = "twin-model-group")
    public void onModelResult(String message) {
        try {
            ModelResult r = objectMapper.readValue(message, ModelResult.class);
            if ("PHYSICAL".equals(r.getSource())) syncService.onModelResult(r);
            else simService.onModelResult(r);
        }
        catch (Exception e) {
            log.error("Failed to process model result: {}", e.getMessage());
        }
    }
}

package com.bioreactordt.digitaltwinservice.kafka;

import com.bioreactordt.digitaltwinservice.models.bioreactorModelResult;
import com.bioreactordt.digitaltwinservice.services.simulationService;
import com.bioreactordt.digitaltwinservice.services.synchroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class bioreactorModelResultConsumer {

    private final ObjectMapper mapper;
    private final simulationService simSerivce;
    private final synchroService synchroService;

    @KafkaListener(topics = "model-results", groupId = "twin-model-group")
    public void onModelResult(String message) {
        try {
            bioreactorModelResult r = mapper.readValue(message, bioreactorModelResult.class);
            if ("PHYSICAL".equals(r.getSource())) synchroService.onModelResult(r);
            else simSerivce.onModelResult(r);
        } catch (Exception e) {
            log.error("Failed to process model result: {}", e.getMessage());
        }
    }

}

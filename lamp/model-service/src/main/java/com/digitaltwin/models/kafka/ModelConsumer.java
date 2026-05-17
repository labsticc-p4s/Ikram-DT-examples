package com.digitaltwin.models.kafka;
import com.digitaltwin.models.model.DataStateModel;
import com.digitaltwin.models.service.LampModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @Slf4j
public class ModelConsumer {

    private final LampModelService service;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "enriched-states", groupId = "model-enriched-group")
    public void onEnriched(String msg) {
        try {
            service.compute(mapper.readValue(msg, DataStateModel.class));
        } catch (Exception e) {
            log.error("Failed to process enriched state: {}", e.getMessage());
        }
    }



}

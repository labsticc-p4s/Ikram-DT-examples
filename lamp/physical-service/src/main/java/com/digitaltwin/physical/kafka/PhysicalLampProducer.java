package com.digitaltwin.physical.kafka;
import com.digitaltwin.physical.model.LampState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class PhysicalLampProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(LampState state) {
        try {
            kafkaTemplate.send("lamp-state", state.getLampId(), objectMapper.writeValueAsString(state));
        }
        catch (Exception e) {
            log.error("Failed to send lamp state", e);
        }
    }
}

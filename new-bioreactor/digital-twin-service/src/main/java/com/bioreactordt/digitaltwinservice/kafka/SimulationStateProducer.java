package com.bioreactordt.digitaltwinservice.kafka;


import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimulationStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public void send(){
        try{

        }
        catch (Exception e) {
            log.error("Failed to send sim state");
        }
    }
}

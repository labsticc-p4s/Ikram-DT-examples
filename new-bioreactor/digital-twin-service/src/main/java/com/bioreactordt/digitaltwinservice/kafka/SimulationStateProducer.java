package com.bioreactordt.digitaltwinservice.kafka;


import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@Component
@RequiredArgsConstructor
@Slf4j
public class SimulationStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);


    public void sendBioreactorState(String experimentId, String reactorId, double ph, double temperature) {
        try {
            Map<String, Object> state = new HashMap<>();
            state.put("experimentId", experimentId);
            state.put("reactorId", reactorId);
            state.put("sensorReadings", Map.of("ph", ph, "temperature", temperature));
            kafkaTemplate.send("bioreactor-state-normalized", experimentId, objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            log.error("Failed to send simulated bioreactor state: {}", e.getMessage());
        }
    }


    public String sendExperimentCreated(Simulation sim) {
        String experimentId = UUID.randomUUID().toString();

        try {
            Map<String, Object> exp = new HashMap<>();
            exp.put("experimentId", experimentId);
            exp.put("reactorId", sim.getReactorId());
            exp.put("condInit", sim.getCondInit());
            exp.put("populationModel", sim.getPopulationModel());
            exp.put("phModel", sim.getPhModel());
            exp.put("tempModel", sim.getTempModel());
            exp.put("source", "sim");

            kafkaTemplate.send("experiment-created", objectMapper.writeValueAsString(exp));

            return experimentId;
        } catch (Exception e) {
            log.error("Failed to send experiment-created: {}", e.getMessage());
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
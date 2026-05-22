package com.bioreactordt.digitaltwinservice.kafka;


import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


@Component
@RequiredArgsConstructor
@Slf4j
public class SimulationStateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public void sendBioreactorState(String experimentId, String reactorId, double ph, double temperature) {
        try {
            Map<String, Object> state = new HashMap<>();
            state.put("experimentId", experimentId);
            state.put("reactorId", reactorId);
            state.put("sensorReadings", Map.of("ph", ph, "temperature", temperature));
            kafkaTemplate.send("bioreactor-state-normalized", reactorId, objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            log.error("Failed to send simulated bioreactor state: {}", e.getMessage());
        }
    }


    public String sendExperimentCreated(Simulation sim, Consumer<String> onReady) {
        try {
            String experimentId = UUID.randomUUID().toString();
            Map<String, Object> exp = new HashMap<>();
            exp.put("experimentId", experimentId);
            exp.put("reactorId", sim.getReactorId());
            exp.put("condInit", sim.getCondInit());
            exp.put("populationModel", sim.getPopulationModel());
            exp.put("phModel", sim.getPhModel());
            exp.put("tempModel", sim.getTempModel());
            exp.put("source", "sim");

            kafkaTemplate.send("experiment-created", objectMapper.writeValueAsString(exp))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send experiment-created: {}", ex.getMessage());
                    } else {
                        sleep(2000);
                        onReady.accept(experimentId);
                    }
            });
            return experimentId;
        } catch (Exception e) {
            log.error("Failed to send experiment-created: {}", e.getMessage());
            return null;
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

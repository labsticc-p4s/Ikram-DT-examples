package com.bioreactordt.experimentservice.services;


import com.bioreactordt.experimentservice.models.Experimentation;
import com.bioreactordt.experimentservice.models.ExperimentationState;
import com.fasterxml.jackson.databind.ObjectMapper;
import greycat.GreyCat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExperimentService {

    private final String greycatUrl;
    private GreyCat greycat;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;



    public ExperimentService(@Value("${greycat.url}") String greycatUrl,  KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.greycatUrl = greycatUrl;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }


    private synchronized GreyCat greycat() {
        if (greycat == null) {
            try {
                greycat = new GreyCat(greycatUrl, null, false, false);
                log.info("Connected to GreyCat at {}", greycatUrl);
            } catch (Exception e) {
                log.error("Failed to connect to GreyCat at {}: {}", greycatUrl, e.getMessage());
                throw new RuntimeException("Failed to connect to GreyCat at " + greycatUrl, e);
            }
        }
        return greycat;
    }



    public void createExperiment(Experimentation ex) {
        try {
            greycat().call("project::experiment_save", ex.getExperimentId(), ex.getReactorId(), ex.getCondInit(), ex.getPopulationModel(), ex.getPhModel(), ex.getTempModel(), ex.getSource());
            log.info("Experiment created");

            kafkaTemplate.send("experiment-started", objectMapper.writeValueAsString(ex)); //send to  model service
            log.info("islam i love u {}",ex);

        } catch (Exception e) {
            log.error("Failed to save experiment {}: {}", ex, e.getMessage());
        }
    }

    public void createExperimentState( ExperimentationState s) {
        try {
            greycat().call("project::experiment_state_save", s.getExperimentId(), s.getPh(), s.getTemperature(), s.getPopulation(), s.getMu(), s.getGammaPh(), s.getGammaTemp(), s.getGrowthStatus());

            log.info("Experiment state created");

        } catch (Exception e) {
            log.error("Failed to push experiment state to GreyCat: {}", e.getMessage());
        }
    }


    public Map<String, Object> getExperimentWithStates(String experimentId) {
        try {
            Object[] raw = (Object[]) greycat().call("project::get_experiment_with_states", experimentId);
            if (raw == null) return null;

            List<Map<String, Object>> states = new ArrayList<>();
            Object[] rawStates = (Object[]) raw[6];
            if (rawStates != null) {
                for (Object rawState : rawStates) {
                    Object[] s = (Object[]) rawState;
                    Map<String, Object> state = new LinkedHashMap<>();
                    state.put("timestamp",    s[0]);
                    state.put("ph",           s[1]);
                    state.put("temperature",  s[2]);
                    state.put("population",   s[3]);
                    state.put("mu",           s[4]);
                    state.put("gammaPh",      s[5]);
                    state.put("gammaTemp",    s[6]);
                    state.put("growthStatus", s[7]);
                    states.add(state);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("experimentId",    raw[0]);
            result.put("reactorId",       raw[1]);
            result.put("populationModel", raw[2]);
            result.put("phModel",         raw[3]);
            result.put("tempModel",       raw[4]);
            result.put("source",          raw[5]);
            result.put("states",          states);

            return result;

        } catch (Exception e) {
            log.error("Failed to get experiment {}: {}", experimentId, e.getMessage());
            return null;
        }
    }

}

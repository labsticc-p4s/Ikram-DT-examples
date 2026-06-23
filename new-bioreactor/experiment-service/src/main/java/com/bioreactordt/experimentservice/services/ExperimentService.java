package com.bioreactordt.experimentservice.services;

import com.bioreactordt.shared.GreycatClient;
import com.bioreactordt.experimentservice.models.Experimentation;
import com.bioreactordt.experimentservice.models.ExperimentationState;
import com.fasterxml.jackson.databind.ObjectMapper;
import greycat.gc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExperimentService {

    private final GreycatClient greycat;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    private final java.util.concurrent.ConcurrentHashMap<String, Map<String, Object>> experimentCache = new java.util.concurrent.ConcurrentHashMap<>();



    public void createExperiment(Experimentation ex) throws Exception {
        greycat.call("project::experiment_save",
                ex.getExperimentId(), ex.getReactorId(), ex.getCondInit(),
                ex.getPopulationModel(), ex.getPhModel(), ex.getTempModel(), ex.getSource());
        log.info("experiment saved: {}", ex.getExperimentId());
        kafkaTemplate.send("experiment-started", objectMapper.writeValueAsString(ex));
    }



    public void createExperimentState(ExperimentationState s) {
        try {
            greycat.call("project::experiment_state_save",
                    s.getExperimentId(), s.getPh(), s.getTemperature(),
                    s.getPopulation(), s.getMu(), s.getGammaPh(), s.getGammaTemp(), s.getGrowthStatus());
        } catch (Exception e) {
            log.warn("state save skipped for {}: {}", s.getExperimentId(), e.getMessage());
        }
    }


    public Map<String, Object> getExperimentWithStates(String experimentId) throws Exception {
        Map<String, Object> exp = experimentCache.get(experimentId);
        if (exp == null) {
            exp = greycat.callMap("project::get_experiment_with_states", experimentId);
            if (exp == null) return null;
            experimentCache.put(experimentId, exp);
        }

        List<Map<String, Object>> states = greycat.callList("project::get_experiment_states", experimentId);

        Map<String, Object> result = new java.util.LinkedHashMap<>(exp);
        result.put("states", states);
        return result;
    }



    public Map<String, Object> getLastExperimentState(String experimentId) throws Exception {
        return greycat.callMap("project::get_last_experiment_state", experimentId);
    }


    public List<Map<String, Object>> getAllExperiments() throws Exception {
        return greycat.callList("project::get_all_experiments");
    }


}
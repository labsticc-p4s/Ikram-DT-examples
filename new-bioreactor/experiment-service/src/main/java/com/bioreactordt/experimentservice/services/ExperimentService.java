package com.bioreactordt.experimentservice.services;


import com.bioreactordt.experimentservice.models.Experimentation;
import com.bioreactordt.experimentservice.models.ExperimentationState;
import com.fasterxml.jackson.databind.ObjectMapper;
import greycat.GreyCat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
            String source = "Physical";
            greycat().call("project::experiment_save", ex.getReactorId(), ex.getCondInit(), ex.getPopulationModel(), ex.getPhModel(), ex.getTempModel(), source);
            log.info("Experiment created");

            kafkaTemplate.send("experiment-started", objectMapper.writeValueAsString(ex)); //send to  model service


        } catch (Exception e) {
            log.error("Failed to save experiment {}: {}", ex, e.getMessage());
        }
    }

    public void createExperimentState(String reactorId, ExperimentationState s) {
        try {
            greycat().call("project::experiment_state_save", reactorId, s.getPh(), s.getTemperature(), s.getPopulation(), s.getMu(), s.getGammaPh(), s.getGammaTemp(), s.getGrowthStatus());

            log.info("Experiment state created");

        } catch (Exception e) {
            log.error("Failed to push experiment state to GreyCat: {}", e.getMessage());
        }
    }

}

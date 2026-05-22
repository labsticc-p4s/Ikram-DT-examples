package com.bioreactordt.gatewayservice.services;

import com.bioreactordt.gatewayservice.models.Experimentation;
import com.bioreactordt.gatewayservice.models.InitialStrain;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${services.strain-url}")
    private String strainUrl;

    private final ConcurrentHashMap<String, String> activeExperiments = new ConcurrentHashMap<>();

    public void startTwin(Experimentation request) throws Exception {

        InitialStrain strain = getInitStrain(request.getCondInit());

        Experimentation experiment = Experimentation.builder()
                .reactorId(request.getReactorId())
                .condInit(strain.getCondId())
                .populationModel(request.getPopulationModel())
                .phModel(request.getPhModel())
                .tempModel(request.getTempModel())
                .source("Physical")
                .build();

        experiment.setExperimentId(UUID.randomUUID().toString());
        activeExperiments.put(experiment.getReactorId(), experiment.getExperimentId());

        kafkaTemplate.send("experiment-created", objectMapper.writeValueAsString(experiment));

        log.info("Twin started reactor={} condId={}", experiment.getReactorId(), experiment.getCondInit());
    }

    public String getActivePhysicalExp(String reactorId){
        return activeExperiments.get(reactorId);
    }


    private InitialStrain getInitStrain(String condId) {
        String url = strainUrl + "/api/strain/initial/" + condId;
        InitialStrain strain = restTemplate.getForObject(url, InitialStrain.class);
        if (strain == null) throw new IllegalArgumentException("InitialStrain not found: " + condId);
        return strain;
    }

}

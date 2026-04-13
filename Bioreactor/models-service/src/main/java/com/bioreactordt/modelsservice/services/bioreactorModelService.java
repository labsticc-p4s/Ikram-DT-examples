package com.bioreactordt.modelsservice.services;

import com.bioreactordt.modelsservice.calculators.pH.*;
import com.bioreactordt.modelsservice.calculators.temperature.*;
import com.bioreactordt.modelsservice.calculators.population.*;
import com.bioreactordt.modelsservice.kafka.BioreactorModelResultProducer;
import com.bioreactordt.modelsservice.manager.*;
import com.bioreactordt.modelsservice.models.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BioreactorModelService {

    private final ModelManager modelManager;
    private final ModelCoordinator coordinator;
    private final BioreactorModelResultProducer  producer;

    public void compute(BioreactorState state) {

        //for now ew work with one strain the mutilple strains in manager is not valid
        CompromiseConditions c = modelManager.resolve(state.getStrainIds());



        // pH
        double gammaPh   = coordinator.getPhModel(state.getReactorId())
                                      .calculate(state.getPh(), c.getPhMin(), c.getPhOpt(), c.getPhMax());

        //temperature
        double gammaTemp = coordinator.getTempModel(state.getReactorId())
                                      .calculate(state.getTemperature(), c.getTempMin(), c.getTempOpt(), c.getTempMax());

        //growth rate
        double mu = c.getMuMax() * gammaPh * gammaTemp;

        //population growth
        PopulationFunction.PopulationResult pop = coordinator.getPopModel(state.getReactorId())
                                                              .calculate(state.getReactorId(), state.getHours(), mu, c.getPopulationInit(), c.getPopulationMax(), c.getLatency());



        producer.send(BioreactorModelResult.builder()
                .reactorId(state.getReactorId())
                .source(state.getSource())
                .ph(state.getPh())
                .temperature(state.getTemperature())
                .population(pop.population())
                .elapsedHours(pop.elapsedHours())
                .gammaPh(round2(gammaPh))
                .gammaTemp(round2(gammaTemp))
                .mu(round4(mu))
                .growthStatus(pop.phase())
                .strainIds(c.getStrainIds())
                .build());
    }

    private double round2(double v) { return Math.round(v * 100.0)   / 100.0; }
    private double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }



}

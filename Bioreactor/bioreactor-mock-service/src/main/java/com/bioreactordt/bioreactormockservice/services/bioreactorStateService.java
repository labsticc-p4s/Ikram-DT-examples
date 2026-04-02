package com.bioreactordt.bioreactormockservice.services;

import com.bioreactordt.bioreactormockservice.kafka.bioreactorProducer;
import com.bioreactordt.bioreactormockservice.models.bioreactorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class bioreactorStateService {

    private final bioreactorProducer producer;

    @Value("${reactor.id}")
    private String reactorId;

    @Value("${reactor.config.population-init}")
    private double populationInit;

    private double ph          = 7.0;
    private double temperature = 37.0;
    private double population;
    private boolean initialized = false;

    @Scheduled(fixedRate = 1000)
    public void update() {
        if (!initialized) {
            population  = populationInit;
            initialized = true;
        }
        producer.send(buildState());
    }

    public bioreactorState buildState() {
        return bioreactorState.builder()
                .reactorId(reactorId)
                .ph(Math.round(ph * 100.0) / 100.0)
                .temperature(Math.round(temperature * 100.0) / 100.0)
                .population(Math.round(population))
                .hours(1.0 / 3600.0)
                .build();
    }

    public bioreactorState setPH(double ph) {
        this.ph = Math.max(0, Math.min(14, ph));
        return buildState();
    }

    public bioreactorState setTemperature(double temp) {
        this.temperature = temp;
        return buildState();
    }

    public bioreactorState getState() {
        return buildState();
    }

    public void updatePopulation(double newPopulation) {
        this.population = newPopulation;
    }

}

package com.bioreactordt.digitaltwinservice.services;

import com.bioreactordt.digitaltwinservice.kafka.SimulationStateProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final SimulationStateProducer producer;





}

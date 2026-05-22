package com.bioreactordt.digitaltwinservice.controllers;

import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.bioreactordt.digitaltwinservice.services.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;


    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody Simulation sim) {
        String experimentId = simulationService.startSimulation(sim);
        return ResponseEntity.accepted().body("Simulation started with " + experimentId);
    }
}

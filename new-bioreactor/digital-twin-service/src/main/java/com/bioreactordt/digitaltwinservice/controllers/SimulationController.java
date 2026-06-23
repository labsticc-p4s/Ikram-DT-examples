package com.bioreactordt.digitaltwinservice.controllers;

import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.bioreactordt.digitaltwinservice.services.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;


    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody Simulation sim) {
        String experimentId = simulationService.startSimulation(sim);
        return ResponseEntity.accepted().body("Simulation started with " + experimentId);
    }
}

package com.bioreactordt.gatewayservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProxyController {

    private final RestTemplate restTemplate;

    @Value("${services.physical-url}") private String physicalUrl;
    @Value("${services.strain-url}")   private String strainUrl;
    @Value("${services.twin-url}")     private String twinUrl;
    @Value("${services.models-url}")   private String modelsUrl;

    @Value("${services.experiment-url}") private String experimentUrl;

    //physical routes
    @GetMapping("/api/physical/state")
    public ResponseEntity<?> physicalState() {
        return restTemplate.getForEntity(physicalUrl + "/api/physical/state", Object.class);
    }

    @PostMapping("/api/physical/register")
    public ResponseEntity<?> physicalRegister() {
        return restTemplate.postForEntity(physicalUrl + "/api/physical/register", null, Object.class);
    }

    //strain routes
    @GetMapping("/api/strain/family")
    public ResponseEntity<?> getFamilies() {
        return restTemplate.getForEntity(strainUrl + "/api/strain/family", Object.class);
    }

    @GetMapping("/api/strain/initial")
    public ResponseEntity<?> getInitials() {
        return restTemplate.getForEntity(strainUrl + "/api/strain/initial", Object.class);
    }

    @PostMapping("/api/strain/family")
    public ResponseEntity<?> saveFamily(@RequestBody Object body) {
        return restTemplate.postForEntity(strainUrl + "/api/strain/family", body, Object.class);
    }

    @PostMapping("/api/strain/initial")
    public ResponseEntity<?> saveInitial(@RequestBody Object body) {
        return restTemplate.postForEntity(strainUrl + "/api/strain/initial", body, Object.class);
    }

    //experiment routes
    @GetMapping("/api/experiments")
    public ResponseEntity<?> getAllExperiments() {
        return restTemplate.getForEntity(experimentUrl + "/api/experiments", Object.class);
    }

    @GetMapping("/api/experiments/{id}")
    public ResponseEntity<?> getExperiment(@PathVariable String id) {
        return restTemplate.getForEntity(experimentUrl + "/api/experiments/" + id, Object.class);
    }

    @GetMapping("/api/experiments/{id}/last-state")
    public ResponseEntity<?> getExperimentLastState(@PathVariable String id) {
        return restTemplate.getForEntity(experimentUrl + "/api/experiments/" + id + "/last-state", Object.class);
    }

    @PostMapping("/api/experiments/create")
    public ResponseEntity<?> createExperiment(@RequestBody Object body) {
        return restTemplate.postForEntity(experimentUrl + "/api/experiments/create", body, Object.class);
    }

    //sim routes
    @PostMapping("/api/simulation/start")
    public ResponseEntity<?> startSimulation(@RequestBody Object body) {
        return restTemplate.postForEntity(twinUrl + "/api/simulation/start", body, String.class);
    }
}
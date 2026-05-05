package com.bioreactordt.experimentservice.controllers;

import com.bioreactordt.experimentservice.models.Experimentation;
import com.bioreactordt.experimentservice.services.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/experiments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ExperimentController {

    private final ExperimentService service;

    @PostMapping("/create")
    public ResponseEntity<?> startExperiment(@RequestBody Experimentation ex) {
        try{
            service.createExperiment(ex);
            return ResponseEntity.ok(Map.of("message", "Registration successed", "status", "success"));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }

    }

}

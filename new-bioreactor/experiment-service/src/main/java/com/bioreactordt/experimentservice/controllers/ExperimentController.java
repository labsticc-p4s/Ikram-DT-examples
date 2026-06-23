package com.bioreactordt.experimentservice.controllers;

import com.bioreactordt.experimentservice.models.Experimentation;
import com.bioreactordt.experimentservice.services.ExperimentService;
import greycat.gc;
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


    //mot used only to test inpostman
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


    @GetMapping("/{experimentId}")
    public ResponseEntity<?> getExperiment(@PathVariable String experimentId) {
        try {
            Map<String, Object> result = service.getExperimentWithStates(experimentId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
           // log.info("workin"+ result);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }



    @GetMapping("/{experimentId}/last-state")
    public ResponseEntity<?> getLastState(@PathVariable String experimentId) {
        try {
            Map<String, Object> state = service.getLastExperimentState(experimentId);
            if (state == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllExperiments() {
        try {
            return ResponseEntity.ok(service.getAllExperiments());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

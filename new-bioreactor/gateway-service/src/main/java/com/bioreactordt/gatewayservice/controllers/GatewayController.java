package com.bioreactordt.gatewayservice.controllers;


import com.bioreactordt.gatewayservice.models.Experimentation;
import com.bioreactordt.gatewayservice.services.GatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/twin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class GatewayController {

    private final GatewayService service;

    @PostMapping("/start")
    public ResponseEntity<?> startTwin(@RequestBody Experimentation request) {
        try {
            String experimentId = service.startTwin(request);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Twin started", "experimentId", experimentId));
        } catch (Exception e) {
            log.error("Failed to start twin: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopTwin(@RequestBody Map<String, String> body) {
        try {
            String reactorId = body.get("reactorId");
            service.stopTwin(reactorId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Twin stopped"));
        } catch (Exception e) {
            log.error("Failed to stop twin: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    //not used
    @GetMapping("/active/{reactorId}")
    public ResponseEntity<?> getActive(@PathVariable String reactorId) {
        String expId = service.getActivePhysicalExp(reactorId);
        if (expId == null) return ResponseEntity.ok(Map.of("active", false));
        return ResponseEntity.ok(Map.of("active", true, "experimentId", expId));
    }
}


package com.bioreactordt.bioreactormockservice.controllers;

import com.bioreactordt.bioreactormockservice.models.bioreactorState;
import com.bioreactordt.bioreactormockservice.services.bioreactorStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/physical/reactor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class bioreactorStateController {

    private final bioreactorStateService service;


    @GetMapping("/state")
    public ResponseEntity<bioreactorState> state() {
        return ResponseEntity.ok(service.getState());
    }

    // replaces setPH + setTemp — now controls replay speed
    @PostMapping("/speed")
    public ResponseEntity<Map<String, Object>> setSpeed(@RequestBody Map<String, Long> body) {
        long factor = body.getOrDefault("speedFactor", 1L);
        service.setSpeedFactor(factor);
        return ResponseEntity.ok(Map.of("speedFactor", factor));
    }

    @GetMapping("/speed")
    public ResponseEntity<Map<String, Object>> getSpeed() {
        return ResponseEntity.ok(Map.of("speedFactor", service.getSpeedFactor()));
    }

}

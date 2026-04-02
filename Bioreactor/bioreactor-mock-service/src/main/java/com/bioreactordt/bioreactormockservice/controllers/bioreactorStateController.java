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

    @PostMapping("/env/ph")
    public ResponseEntity<bioreactorState> setPH(@RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(service.setPH(body.get("ph")));
    }

    @PostMapping("/env/temperature")
    public ResponseEntity<bioreactorState> setTemp(@RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(service.setTemperature(body.get("temperature")));
    }

    @PostMapping("/twin-command")
    public ResponseEntity<bioreactorState> twinCommand(@RequestBody Map<String, Double> body) {
        if (body.containsKey("ph"))          service.setPH(body.get("ph"));
        if (body.containsKey("temperature")) service.setTemperature(body.get("temperature"));
        return ResponseEntity.ok(service.getState());
    }
}

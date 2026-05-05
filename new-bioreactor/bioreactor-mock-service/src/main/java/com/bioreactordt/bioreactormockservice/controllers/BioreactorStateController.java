package com.bioreactordt.bioreactormockservice.controllers;

import com.bioreactordt.bioreactormockservice.models.BioreactorEntity;
import com.bioreactordt.bioreactormockservice.models.BioreactorState;
import com.bioreactordt.bioreactormockservice.services.BioreactorEntityService;
import com.bioreactordt.bioreactormockservice.services.BioreactorStateSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/physical")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BioreactorStateController {

    private final BioreactorStateSendService service;
    private final BioreactorEntityService bioreactorService;


    @Value("${reactor.id}")   private String reactorId;

    @GetMapping
    public ResponseEntity<BioreactorEntity> info() {
        return ResponseEntity.ok(BioreactorEntity.builder().reactorId(reactorId).build());
    }

    @GetMapping("/state")
    public ResponseEntity<BioreactorState> state() {
        return ResponseEntity.ok(service.currentState());
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerBioreactor() {
        try {
            bioreactorService.register();
            return ResponseEntity.ok(Map.of("message", "Registration successed", "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage(), "status", "failed"
            ));
        }
    }




}

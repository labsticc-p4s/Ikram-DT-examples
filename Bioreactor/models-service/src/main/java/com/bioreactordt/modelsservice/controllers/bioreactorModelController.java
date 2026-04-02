package com.bioreactordt.modelsservice.controllers;

import com.bioreactordt.modelsservice.models.bioreactorConfig;
import com.bioreactordt.modelsservice.services.bioreactorModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class bioreactorModelController {

    private final bioreactorModelService service;

    @GetMapping("/config/{reactorId}")
    public ResponseEntity<bioreactorConfig> getConfig(@PathVariable String reactorId) {
        return ResponseEntity.ok(service.getConfig_public(reactorId));
    }
}

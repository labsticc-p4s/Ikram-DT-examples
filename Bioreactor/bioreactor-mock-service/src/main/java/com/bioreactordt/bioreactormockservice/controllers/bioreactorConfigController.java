package com.bioreactordt.bioreactormockservice.controllers;

import com.bioreactordt.bioreactormockservice.models.bioreactorConfig;
import com.bioreactordt.bioreactormockservice.services.bioreactorConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/physical/config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class bioreactorConfigController {

    private final bioreactorConfigService service;

    @GetMapping
    public ResponseEntity<bioreactorConfig> get() {
        return service.get().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<bioreactorConfig> getById(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}

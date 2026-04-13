package com.bioreactordt.strainservice.controllers;

import com.bioreactordt.strainservice.models.StrainConfig;
import com.bioreactordt.strainservice.services.StrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/strain")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StrainController {

    private final StrainService service;

    @GetMapping("/{strainId}")
    public ResponseEntity<StrainConfig> getById(@PathVariable String strainId) {
        return service.getById(strainId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reactor/{reactorId}")
    public ResponseEntity<List<StrainConfig>> getByReactor(@PathVariable String reactorId) {
        return ResponseEntity.ok(service.getByBioreactor(reactorId));
    }


    @GetMapping("/reactor/{reactorId}/ids")
    public ResponseEntity<List<String>> getStrainIds(@PathVariable String reactorId) {
        return ResponseEntity.ok(service.getStrainIds(reactorId));
    }

    @GetMapping
    public ResponseEntity<List<StrainConfig>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<StrainConfig> register(@RequestBody StrainConfig strain) {
        service.add(strain);
        return ResponseEntity.ok(strain);
    }

    @DeleteMapping("/{strainId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String strainId) {
        service.delete(strainId);
        return ResponseEntity.ok(Map.of("deleted", strainId));
    }


}

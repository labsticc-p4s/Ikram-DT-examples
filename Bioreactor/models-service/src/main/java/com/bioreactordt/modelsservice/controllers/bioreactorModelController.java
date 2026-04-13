package com.bioreactordt.modelsservice.controllers;

import com.bioreactordt.modelsservice.manager.ModelCoordinator;
import com.bioreactordt.modelsservice.manager.ModelManager;
import com.bioreactordt.modelsservice.models.ModelSelection;
import com.bioreactordt.modelsservice.models.StrainConfig;
import com.bioreactordt.modelsservice.services.BioreactorModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BioreactorModelController {

    private final ModelCoordinator coordinator;
    private final ModelManager modelManager;


    @GetMapping("/available")
    public ResponseEntity<Map<String, List<String>>> available() {
        return ResponseEntity.ok(Map.of(
                "ph",          coordinator.availablePhModels(),
                "temperature", coordinator.availableTempModels(),
                "population",  coordinator.availablePopModels()
        ));
    }

    @GetMapping("/{reactorId}")
    public ResponseEntity<ModelSelection> getSelection(@PathVariable String reactorId) {
        return ResponseEntity.ok(coordinator.getSelection(reactorId));
    }

    @PostMapping("/select")
    public ResponseEntity<ModelSelection> select(@RequestBody ModelSelection selection) {
        coordinator.select(selection);
        return ResponseEntity.ok(selection);

    }

    @DeleteMapping("/{reactorId}")
    public ResponseEntity<Map<String, String>> reset(@PathVariable String reactorId) {
        coordinator.reset(reactorId);
        return ResponseEntity.ok(Map.of("reset", reactorId));
    }

    @PostMapping("/cache/invalidate/{strainId}")
    public ResponseEntity<Map<String, String>> invalidateCache(@PathVariable String strainId) {
        modelManager.invalidateStrainCache(strainId);
        return ResponseEntity.ok(Map.of("invalidated", strainId));
    }
}
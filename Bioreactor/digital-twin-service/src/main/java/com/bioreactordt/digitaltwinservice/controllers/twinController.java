package com.bioreactordt.digitaltwinservice.controllers;

import com.bioreactordt.digitaltwinservice.models.bioreactorModelResult;
import com.bioreactordt.digitaltwinservice.models.simulation;
import com.bioreactordt.digitaltwinservice.services.simulationService;
import com.bioreactordt.digitaltwinservice.services.synchroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class twinController {

    private final simulationService simSerivce;
    private final synchroService synchroService;

    @GetMapping("/state")
    public ResponseEntity<bioreactorModelResult> state() {
        bioreactorModelResult r = synchroService.getModelResult();
        return r != null ? ResponseEntity.ok(r) : ResponseEntity.noContent().build();
    }

    @PostMapping("/command")
    public ResponseEntity<Map<String, Object>> command(@RequestBody Map<String, Double> body) {
        synchroService.sendCommandToPhysical(
                body.getOrDefault("ph", 7.0),
                body.getOrDefault("temperature", 37.0));
        return ResponseEntity.ok(Map.of("sent", true));
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncStatus() {
        return ResponseEntity.ok(Map.of(
                "twinned",    synchroService.isTwinned(),
                "reactorId",  synchroService.getReactorId() != null ? synchroService.getReactorId() : ""
        ));
    }

    @PostMapping("/sync/enable")
    public ResponseEntity<Map<String, Object>> enableSync(@RequestBody Map<String, String> body) {
        String id = body.getOrDefault("reactorId", "BIOREACTOR-001");
        synchroService.twinOrUntwin(true, id);
        return ResponseEntity.ok(Map.of("twinned", true, "reactorId", id));
    }

    @PostMapping("/sync/disable")
    public ResponseEntity<Map<String, Object>> disableSync() {
        synchroService.twinOrUntwin(false, null);
        return ResponseEntity.ok(Map.of("twinned", false));
    }

    @PostMapping("/simulations")
    public ResponseEntity<Map<String, String>> start(@RequestBody simulation sc) {
        return ResponseEntity.ok(Map.of("simId", simSerivce.start(sc), "status", "STARTED"));
    }

    @GetMapping("/simulations")
    public ResponseEntity<List<simulation>> all() {
        return ResponseEntity.ok(simSerivce.getAll());
    }


    @PostMapping("/simulations/{id}/pause")
    public ResponseEntity<Map<String, String>> pause(@PathVariable String id) {
        return simSerivce.pause(id)
                ? ResponseEntity.ok(Map.of("simId", id, "status", "PAUSED"))
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/simulations/{id}/resume")
    public ResponseEntity<Map<String, String>> resume(@PathVariable String id) {
        return simSerivce.resume(id)
                ? ResponseEntity.ok(Map.of("simId", id, "status", "RUNNING"))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/simulations/{id}")
    public ResponseEntity<Map<String, String>> stop(@PathVariable String id) {
        return simSerivce.stop(id)
                ? ResponseEntity.ok(Map.of("simId", id, "status", "STOPPED"))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/simulations")
    public ResponseEntity<Map<String, Object>> stopAll() {
        return ResponseEntity.ok(Map.of("stopped", simSerivce.stopAll()));
    }




}

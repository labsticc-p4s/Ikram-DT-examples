package com.digitaltwin.twin.controller;

import com.digitaltwin.twin.model.ModelResult;
import com.digitaltwin.twin.model.Simulation;
import com.digitaltwin.twin.service.SimulationService;
import com.digitaltwin.twin.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/twin") @RequiredArgsConstructor @CrossOrigin(origins="*")
public class DigitalTwinController {

    private final SyncService syncService;
    private final SimulationService simService;

    @GetMapping("/state")
    public ResponseEntity<ModelResult> state() {
        ModelResult r = syncService.getModelResult();
        return r != null ? ResponseEntity.ok(r) : ResponseEntity.noContent().build();
    }

    @PostMapping("/command")
    public ResponseEntity<Map<String,String>> command(@RequestBody Map<String,String> body) {
        syncService.sendCommandToPhysical(body.getOrDefault("action",""));
        return ResponseEntity.ok(Map.of("sent", body.getOrDefault("action","")));
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String,Object>> syncStatus() {
        return ResponseEntity.ok(Map.of(
                "twinned",      syncService.isTwinned(),
                "twinedLampId", syncService.getTwinedLampId() != null ? syncService.getTwinedLampId() : ""
        ));
    }

    // enable sync with a specific lamp — lampId passed in body
    @PostMapping("/sync/enable")
    public ResponseEntity<Map<String,Object>> enableSync(@RequestBody Map<String,String> body) {
        String lampId = body.getOrDefault("lampId", "LAMP-001");
        syncService.twinOrUntwin(true, lampId);
        return ResponseEntity.ok(Map.of("twinned", true, "lampId", lampId));
    }

    @PostMapping("/sync/disable")
    public ResponseEntity<Map<String,Object>> disableSync() {
        syncService.twinOrUntwin(false, null);
        return ResponseEntity.ok(Map.of("twinned", false));
    }

    @PostMapping("/simulations")
    public ResponseEntity<Map<String,String>> start(@RequestBody Simulation sc) {
        return ResponseEntity.ok(Map.of("simId", simService.start(sc), "status", "STARTED"));
    }

    @GetMapping("/simulations")
    public ResponseEntity<List<Simulation>> all() { return ResponseEntity.ok(simService.getAll()); }

    @PostMapping("/simulations/{id}/pause")
    public ResponseEntity<Map<String,String>> pause(@PathVariable String id) {
        return simService.pause(id) ? ResponseEntity.ok(Map.of("simId",id,"status","PAUSED")) : ResponseEntity.notFound().build();
    }

    @PostMapping("/simulations/{id}/resume")
    public ResponseEntity<Map<String,String>> resume(@PathVariable String id) {
        return simService.resume(id) ? ResponseEntity.ok(Map.of("simId",id,"status","RUNNING")) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/simulations/{id}")
    public ResponseEntity<Map<String,String>> stop(@PathVariable String id) {
        return simService.stop(id) ? ResponseEntity.ok(Map.of("simId",id,"status","STOPPED")) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/simulations")
    public ResponseEntity<Map<String,Object>> stopAll() {
        return ResponseEntity.ok(Map.of("stopped", simService.stopAll()));
    }

    @PostMapping("/simulations/{id}/restart")
    public ResponseEntity<Map<String,String>> restart(@PathVariable String id,
                                                      @RequestBody Simulation sc) {
        simService.stop(id);
        String newId = simService.start(sc);
        return ResponseEntity.ok(Map.of("simId", newId, "status", "STARTED"));
    }
}

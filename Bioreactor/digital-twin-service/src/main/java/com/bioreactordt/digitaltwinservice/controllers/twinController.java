package com.bioreactordt.digitaltwinservice.controllers;

import com.bioreactordt.digitaltwinservice.models.BioreactorModelResult;
import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.bioreactordt.digitaltwinservice.services.simulationService;
import com.bioreactordt.digitaltwinservice.services.synchroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/twin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class twinController {

    private final simulationService simSerivce;
    private final synchroService    synchroService;

    @GetMapping("/state")
    public ResponseEntity<BioreactorModelResult> state() {
        BioreactorModelResult r = synchroService.getModelResult();
        return r != null ? ResponseEntity.ok(r) : ResponseEntity.noContent().build();
    }


    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncStatus() {
        return ResponseEntity.ok(Map.of(
                "twinned",   synchroService.isTwinned(),
                "reactorId", synchroService.getReactorId() != null ? synchroService.getReactorId() : "",
                "strainIds", synchroService.getStrainIds()
        ));
    }

    @PostMapping("/sync/enable")
    public ResponseEntity<Map<String, Object>> enableSync(@RequestBody Map<String, Object> body) {
        String id = (String) body.getOrDefault("reactorId", "BIOREACTOR-001");
        List<String> strainIds = (List<String>) body.getOrDefault("strainIds", List.of());
        synchroService.twinOrUntwin(true, id, strainIds);
        return ResponseEntity.ok(Map.of("twinned", true, "reactorId", id, "strainIds", strainIds));
    }

    @PostMapping("/sync/disable")
    public ResponseEntity<Map<String, Object>> disableSync() {
        synchroService.twinOrUntwin(false, null, List.of());
        return ResponseEntity.ok(Map.of("twinned", false));
    }

    @PostMapping("/simulations")
    public ResponseEntity<Map<String, String>> start(@RequestBody Simulation sc) {
        return ResponseEntity.ok(Map.of("simId", simSerivce.start(sc), "status", "STARTED"));
    }

    @GetMapping("/simulations")
    public ResponseEntity<List<Simulation>> all() {
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
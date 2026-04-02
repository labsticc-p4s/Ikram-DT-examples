package com.bioreactordt.shadowservice.controllers;

import com.bioreactordt.shadowservice.models.bioreactorModelResult;
import com.bioreactordt.shadowservice.services.shadowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shadow")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class shadowController {

    private final shadowService service;

    @GetMapping("/physical/last/{n}")
    public ResponseEntity<List<bioreactorModelResult>> lastN(@PathVariable int n) {
        return ResponseEntity.ok(service.getPhysicalLastN(n));
    }

    @GetMapping("/simulations")
    public ResponseEntity<List<bioreactorModelResult>> simLogs() {
        return ResponseEntity.ok(service.getSimLogs());
    }

    @GetMapping("/simulations/{reactorId}")
    public ResponseEntity<List<bioreactorModelResult>> simHistory(@PathVariable String reactorId) {
        return ResponseEntity.ok(service.getSimHistoryForReactor(reactorId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clear() {
        service.clear();
        return ResponseEntity.ok(Map.of("status", "CLEARED"));
    }

    @PostMapping("/sync/enable")
    public ResponseEntity<Map<String, Object>> enable(@RequestParam String reactorId) {
        service.setTwinned(true, reactorId);
        return ResponseEntity.ok(Map.of("syncEnabled", true, "reactorId", reactorId));
    }

    @PostMapping("/sync/disable")
    public ResponseEntity<Map<String, Object>> disable() {
        service.setTwinned(false, null);
        return ResponseEntity.ok(Map.of("syncEnabled", false));
    }


}

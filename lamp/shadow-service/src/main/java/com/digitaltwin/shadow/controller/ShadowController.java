package com.digitaltwin.shadow.controller;
import com.digitaltwin.shadow.model.ModelResult;
import com.digitaltwin.shadow.service.ShadowStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/shadow")
@RequiredArgsConstructor @CrossOrigin(origins="*")
public class ShadowController {

    private final ShadowStorageService service;


    @GetMapping("/physical/last/{n}") public ResponseEntity<List<ModelResult>> physicallastN(@PathVariable int n) {
        return ResponseEntity.ok(service.getPhysicalLastN(n));
    }

    @GetMapping("/simulations")
    public ResponseEntity<List<ModelResult>> simLogs() {
        return ResponseEntity.ok(service.getSimLogs());
    }

    @GetMapping("/simulations/{lampId}")
    public ResponseEntity<List<ModelResult>> simHistoryForLamp(@PathVariable String lampId) {
        return ResponseEntity.ok(service.getSimHistoryForLamp(lampId));
    }

    @DeleteMapping("/clear") public ResponseEntity<Map<String,String>> clear(){
        service.clear();
        return ResponseEntity.ok(Map.of("status","CLEARED"));
    }

    @PostMapping("/sync/enable")
    public ResponseEntity<Map<String,Object>> enable(@RequestParam String lampId) {
        service.setTwinned(true, lampId);
        return ResponseEntity.ok(Map.of("syncEnabled", true, "lampId", lampId));
    }

    @PostMapping("/sync/disable")
    public ResponseEntity<Map<String,Object>> disable() {
        service.setTwinned(false, null);
        return ResponseEntity.ok(Map.of("syncEnabled", false));
    }

}

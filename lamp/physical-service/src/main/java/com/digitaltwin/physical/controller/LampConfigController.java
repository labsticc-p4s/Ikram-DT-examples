package com.digitaltwin.physical.controller;

import com.digitaltwin.physical.model.LanpConfig;
import com.digitaltwin.physical.service.LampConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/physical/registry")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LampConfigController {

    private final LampConfigService configService;

    @GetMapping
    public ResponseEntity<LanpConfig> get() {
        return configService.get()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // get used by model-service and twin-service
    @GetMapping("/{id}")
    public ResponseEntity<LanpConfig> getById(@PathVariable String id) {
        return configService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //add config of a new lamp in the cash in front
    @PostMapping
    public ResponseEntity<LanpConfig> register(@RequestBody LanpConfig lc) {
        configService.addConfLamp(lc);
        return ResponseEntity.ok(lc);
    }

}

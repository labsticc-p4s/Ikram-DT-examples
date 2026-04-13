package com.bioreactordt.gatewayservice.controllers;

import com.bioreactordt.gatewayservice.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class GatewayController {

    @Value("${services.models-url}")
    private String modelsUrl;


    private final EnrichedStateService enrichedService;
    private final RestTemplate restTemplate;




    @PostMapping("/cache/invalidate/{reactorId}")
    public ResponseEntity<Map<String, String>> invalidate(@PathVariable String strainId) {
        enrichedService.invalidateCache("BIOREACTOR-001");
        try {
            restTemplate.postForEntity(
                    modelsUrl + "/api/models/cache/invalidate/" + strainId,
                    null, String.class
            );
            log.info("Models-service cache invalidated for strainId={}", strainId);
        } catch (Exception e) {
            log.warn("Could not invalidate models-service cache for {}: {}", strainId, e.getMessage());
        }

        return ResponseEntity.ok(Map.of("invalidated", strainId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}

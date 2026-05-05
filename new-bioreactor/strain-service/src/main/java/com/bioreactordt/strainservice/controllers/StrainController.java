package com.bioreactordt.strainservice.controllers;

import com.bioreactordt.strainservice.models.InitialStrain;
import com.bioreactordt.strainservice.models.StrainFamily;
import com.bioreactordt.strainservice.services.StrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/strain")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class StrainController {

    private final StrainService service;


    @PostMapping("/initial")
    public ResponseEntity<?> createInitialStrain(@RequestBody InitialStrain initialStrain) {
        try {
            service.saveInitialStrain(initialStrain);
            return ResponseEntity.ok(Map.of("message", "Registration successed", "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/family")
    public ResponseEntity<?> registerFamily(@RequestBody StrainFamily family) {
        try {
            service.saveFamily(family);
            return ResponseEntity.ok(Map.of("message", "Registration successed", "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/initial/{condId}")
    public ResponseEntity<?> getInitialStrain(@PathVariable String condId) {
        try {
            return ResponseEntity.ok(service.getInitialStrain(condId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }




}

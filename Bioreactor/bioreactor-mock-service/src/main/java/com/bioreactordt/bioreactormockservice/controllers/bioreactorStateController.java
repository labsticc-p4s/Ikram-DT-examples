package com.bioreactordt.bioreactormockservice.controllers;

import com.bioreactordt.bioreactormockservice.models.bioreactorState;
import com.bioreactordt.bioreactormockservice.services.bioreactorStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/physical")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BioreactorStateController {

    private final bioreactorStateService service;


    @GetMapping("/state")
    public ResponseEntity<bioreactorState> state() {
        return ResponseEntity.ok(service.getState());
    }






}

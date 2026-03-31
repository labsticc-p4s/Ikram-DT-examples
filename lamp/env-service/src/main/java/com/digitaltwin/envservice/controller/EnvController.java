package com.digitaltwin.envservice.controller;

import com.digitaltwin.envservice.model.EnvState;
import com.digitaltwin.envservice.service.EnvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/env")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnvController {

    private final EnvService service;

    @GetMapping("/room/{roomId}/state")
    public ResponseEntity<EnvState> roomState(@PathVariable String roomId) {
        return ResponseEntity.ok(service.getState(roomId));
    }

    @PostMapping("/room/{roomId}/temp")
    public ResponseEntity<EnvState> setRoomTemp(@PathVariable String roomId,
                                            @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(service.setRoomTemp(roomId, body.get("temperature")));
    }
}

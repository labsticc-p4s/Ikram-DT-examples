package com.digitaltwin.physical.controller;
import com.digitaltwin.physical.model.LampState;
import com.digitaltwin.physical.service.LampPhysicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/physical/lamp")
@RequiredArgsConstructor @CrossOrigin(origins="*")
public class LampPhysicalController {

    private final LampPhysicalService service;

    @GetMapping("/state")
    public ResponseEntity<LampState> state() {
        return ResponseEntity.ok(service.getState());
    }

    @PostMapping("/on")
    public ResponseEntity<LampState> on(@RequestParam(defaultValue="100") double brightness) {
        return ResponseEntity.ok(service.turnOn(brightness));
    }

    @PostMapping("/off")
    public ResponseEntity<LampState> off() {
        return ResponseEntity.ok(service.turnOff());
    }

    @PostMapping("/brightness")
    public ResponseEntity<LampState> brightness(@RequestBody Map<String,Double> b) {
        return ResponseEntity.ok(service.setBrightness(b.getOrDefault("brightness",100.0)));
    }



    @PostMapping("/twin-command")
    public ResponseEntity<LampState> twinCommand(@RequestBody Map<String,Object> b) {
        return switch((String)b.getOrDefault("action","")) {
            case "ON"  -> ResponseEntity.ok(service.turnOn(100));
            case "OFF" -> ResponseEntity.ok(service.turnOff());
            default    -> ResponseEntity.badRequest().build();
        };
    }
}

package com.bioreactordt.digitaltwinservice.services;

import com.bioreactordt.digitaltwinservice.models.bioreactorModelResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class synchroService {

    private final RestTemplate restTemplate;

    @Getter
    private bioreactorModelResult modelResult;
    @Getter
    private volatile boolean twinned    = false;
    @Getter
    private volatile String  reactorId  = null;

    public void twinOrUntwin(boolean enabled, String reactorId) {
        this.twinned   = enabled;
        this.reactorId = enabled ? reactorId : null;
        if (!enabled) modelResult = null;
        log.info("Sync {}: reactorId={}", enabled ? "ENABLED" : "DISABLED", reactorId);
    }

    public void onModelResult(bioreactorModelResult r) {
        if (!twinned) return;
        if (reactorId != null && !reactorId.equals(r.getReactorId())) return;
        modelResult = r;
    }

    public void sendCommandToPhysical(double ph, double temperature) {
        if (reactorId == null) { log.warn("No reactor twinned — command ignored"); return; }
        try {
            restTemplate.postForObject(
                    "http://physical-service:8081/api/physical/reactor/twin-command",
                    Map.of("ph", ph, "temperature", temperature),
                    String.class);
        } catch (Exception e) {
            log.error("Command to physical reactor failed: {}", e.getMessage());
        }
    }


}

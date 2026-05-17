package com.digitaltwin.twin.service;

import com.digitaltwin.twin.model.ModelResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class SyncService {

    private final RestTemplate restTemplate;

    @Getter private volatile ModelResult modelResult;
    @Getter private volatile boolean twinned     = false;
    @Getter private volatile String  twinedLampId = null;

    public void twinOrUntwin(boolean enabled, String lampId) {
        this.twinned     = enabled;
        this.twinedLampId = enabled ? lampId : null;
        if (!enabled) modelResult = null;
        log.info("Sync {}: lampId={}", enabled ? "ENABLED" : "DISABLED", lampId);
    }

    public void onModelResult(ModelResult r) {
        if (!twinned) return;
        if (twinedLampId != null && !twinedLampId.equals(r.getLampId())) return;
        modelResult = r;
    }

    private String physicalBaseUrl(String lampId) {
        int n = Integer.parseInt(lampId.replaceAll("\\D", ""));
        int port = (n == 1) ? 8081 : (n == 2) ? 8091 : 8090 + n;
        return "http://physical-service-" + n + ":" + port;
    }

    //sends on and off command to the currently twinned physical lamp
    public void sendCommandToPhysical(String action) {
        if (twinedLampId == null) { log.warn("No lamp twinned — command ignored"); return; }
        try {
            String url = physicalBaseUrl(twinedLampId) + "/api/physical/lamp/twin-command";
            restTemplate.postForObject(url, Map.of("action", action), String.class);
            log.info("Command {} sent to {}", action, url);
        } catch (Exception e) {
            log.error("Command to {} failed: {}", twinedLampId, e.getMessage());
        }
    }
}

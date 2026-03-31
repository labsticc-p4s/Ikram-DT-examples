package com.digitaltwin.physical.service;

import com.digitaltwin.physical.model.LanpConfig;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LampConfigService {

    private LanpConfig lamp;

    public void addConfLamp(LanpConfig lc){

        this.lamp = lc;
    }

    public Optional<LanpConfig> get() {

        return Optional.ofNullable(lamp);
    }

    public Optional<LanpConfig> get(String id) {
        if (lamp != null && lamp.getLampId().equals(id)) return Optional.of(lamp);
        return Optional.empty();
    }
}

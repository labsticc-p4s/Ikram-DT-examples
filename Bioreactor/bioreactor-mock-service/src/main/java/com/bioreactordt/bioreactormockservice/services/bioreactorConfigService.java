package com.bioreactordt.bioreactormockservice.services;

import com.bioreactordt.bioreactormockservice.models.bioreactorConfig;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class bioreactorConfigService {

    private bioreactorConfig config;

    public void addConf(bioreactorConfig c){
        this.config = c;
    }

    public Optional<bioreactorConfig> get()   {
        return Optional.ofNullable(config); }

    public Optional<bioreactorConfig> get(String id) {
        if (config != null && config.getReactorId().equals(id))
            return Optional.of(config);
        return Optional.empty();
    }
}

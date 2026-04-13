package com.bioreactordt.modelsservice.manager;

import com.bioreactordt.modelsservice.models.CompromiseConditions;
import com.bioreactordt.modelsservice.models.StrainConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelManager {

    private final RestTemplate restTemplate;

    @Value("${services.strain-url}")
    private String strainUrl;

    private final ConcurrentHashMap<String, StrainConfig> strainCache = new ConcurrentHashMap<>();

    public CompromiseConditions resolve(List<String> strainIds) {
        List<StrainConfig> strains = strainIds.stream().map(this::getStrain).filter(Objects::nonNull).toList();

        if (strains.isEmpty())    return defaultConditions(strainIds);
        return oneStrain(strains.get(0), strainIds);
    }

    private CompromiseConditions oneStrain(StrainConfig s, List<String> ids) {
        return CompromiseConditions.builder()
                .phMin(s.getPhMin()).phOpt(s.getPhOpt()).phMax(s.getPhMax())
                .tempMin(s.getTempMin()).tempOpt(s.getTempOpt()).tempMax(s.getTempMax())
                .muMax(s.getMuMax())
                .populationInit(s.getPopulationInit())
                .populationMax(s.getPopulationMax())
                .latency(s.getLatency())
                .strainIds(ids)
                .build();
    }


    private StrainConfig getStrain(String strainId) {
        return strainCache.computeIfAbsent(strainId, id -> {
            try {
                StrainConfig s = restTemplate.getForObject(
                        strainUrl + "/api/strain/" + id, StrainConfig.class);
                return s;
            } catch (Exception e) {
                log.warn("Cannot fetch strain {}: {}", id, e.getMessage());
                strainCache.remove(id);
                return null;
            }
        });
    }

    public void invalidateStrainCache(String strainId) {
        strainCache.remove(strainId);
    }

    private CompromiseConditions defaultConditions(List<String> ids) {
        return CompromiseConditions.builder()
                .phMin(5.0).phOpt(7.0).phMax(9.0)
                .tempMin(25.0).tempOpt(37.0).tempMax(45.0)
                .muMax(0.5).populationInit(1_000_000).populationMax(1e10).latency(0.01)
                .strainIds(ids)
                .build();
    }
}
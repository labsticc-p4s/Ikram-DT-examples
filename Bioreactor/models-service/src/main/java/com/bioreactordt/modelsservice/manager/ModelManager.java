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
        if (strains.size() == 1) return oneStrain(strains.get(0), strainIds);
        return intersectionMultipleStrains(strains, strainIds);
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
                .incompatible(false)
                .build();
    }

    private CompromiseConditions intersectionMultipleStrains(List<StrainConfig> strains, List<String> ids) {
        double phMin   = strains.stream().mapToDouble(StrainConfig::getPhMin).max().orElse(5.0);
        double phMax   = strains.stream().mapToDouble(StrainConfig::getPhMax).min().orElse(9.0);
        double phOpt   = strains.stream().mapToDouble(StrainConfig::getPhOpt).average().orElse(7.0);
        double tempMin = strains.stream().mapToDouble(StrainConfig::getTempMin).max().orElse(25.0);
        double tempMax = strains.stream().mapToDouble(StrainConfig::getTempMax).min().orElse(45.0);
        double tempOpt = strains.stream().mapToDouble(StrainConfig::getTempOpt).average().orElse(37.0);
        double muMax   = strains.stream().mapToDouble(StrainConfig::getMuMax).min().orElse(0.5);
        double N0      = strains.stream().mapToDouble(StrainConfig::getPopulationInit).average().orElse(1_000_000);
        double Nmax    = strains.stream().mapToDouble(StrainConfig::getPopulationMax).min().orElse(1e10);
        double latency = strains.stream().mapToDouble(StrainConfig::getLatency).max().orElse(0.01);

        boolean incompatible = phMin >= phMax || tempMin >= tempMax;
        String  reason       = incompatible
                ? String.format("pH window [%.1f,%.1f] or temp window [%.1f,%.1f] has no overlap",
                phMin, phMax, tempMin, tempMax)
                : null;
        if (incompatible) log.warn("Strain incompatibility: {}", reason);

        phOpt   = Math.max(phMin,   Math.min(phMax,   phOpt));
        tempOpt = Math.max(tempMin, Math.min(tempMax, tempOpt));

        return CompromiseConditions.builder()
                .phMin(phMin).phOpt(phOpt).phMax(phMax)
                .tempMin(tempMin).tempOpt(tempOpt).tempMax(tempMax)
                .muMax(muMax).populationInit(N0).populationMax(Nmax).latency(latency)
                .strainIds(ids).incompatible(incompatible).incompatibilityReason(reason)
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
                .strainIds(ids).incompatible(false)
                .build();
    }
}
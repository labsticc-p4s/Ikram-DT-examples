package com.bioreactordt.gatewayservice.services;
import com.bioreactordt.gatewayservice.models.EnrichedBioreactorState;
import com.bioreactordt.gatewayservice.models.BioreactorState;
import com.bioreactordt.gatewayservice.helpers.ValidationValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichedStateService {
    private final RestTemplate     restTemplate;
    private final ValidationValues validator;

    @Value("${services.strain-url}")
    private String strainUrl;

    private final ConcurrentHashMap<String, CachedStrainIds> strainCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60_000;

    public EnrichedBioreactorState mergeData(BioreactorState s) {
        List<String> strainIds = getStrainsByBioreactorId(s.getReactorId());

        EnrichedBioreactorState enriched = EnrichedBioreactorState.builder()
                .reactorId(s.getReactorId())
                .strainIds(strainIds)
                .ph(s.getPh())
                .temperature(s.getTemperature())
                .hours(s.getHours())
                .build();

        return validator.validate(enriched);
    }

    private List<String> getStrainsByBioreactorId(String reactorId) {
        CachedStrainIds cached = strainCache.get(reactorId);
        if (cached != null && !cached.isExpired()) {
            return cached.strainIds;
        }
        try {
            List<String> ids = restTemplate.exchange(strainUrl + "/api/strain/reactor/" + reactorId + "/ids", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<String>>() {}).getBody();
            if (ids != null && !ids.isEmpty()) {
                strainCache.put(reactorId, new CachedStrainIds(ids));
                log.debug("Get {} strains for reactor {}", ids.size(), reactorId);
                return ids;
            }
        } catch (Exception e) {
            log.warn("Could not get strainIds for {} — {}", reactorId, e.getMessage());
        }
        return List.of();
    }

    public void invalidateCache(String reactorId) {
        strainCache.remove(reactorId);
        log.info("Strain cache invalidated for reactor {}", reactorId);
    }





    private static class CachedStrainIds {
        final List<String> strainIds;
        final long         expiresAt;
        CachedStrainIds(List<String> ids) {
            this.strainIds = ids;
            this.expiresAt = System.currentTimeMillis() + CACHE_TTL_MS;
        }
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }

}

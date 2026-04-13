package com.bioreactordt.strainservice.services;

import com.bioreactordt.strainservice.models.StrainConfig;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StrainService {

    private final ConcurrentHashMap<String, StrainConfig> strains = new ConcurrentHashMap<>(); //for strainId with strainConfig
    private final ConcurrentHashMap<String, List<String>> bioreactorStrains = new ConcurrentHashMap<>(); //for reactorId with their strainId


    public void add(StrainConfig strain) {
        strains.put(strain.getStrainId(), strain);
        bioreactorStrains.computeIfAbsent(strain.getReactorId(), k -> new ArrayList<>()).removeIf(id -> id.equals(strain.getStrainId())); //avoid duplication
        bioreactorStrains.get(strain.getReactorId()).add(strain.getStrainId());
    }

    public Optional<StrainConfig> getById(String strainId) {
        return Optional.ofNullable(strains.get(strainId));
    }

    //get strains by bioreactor id
    public List<StrainConfig> getByBioreactor(String reactorId) {
        List<String> ids = bioreactorStrains.getOrDefault(reactorId, List.of());
        return ids.stream().map(strains::get).filter(Objects::nonNull).toList();
    }

    //get strain ids by bioreactor id
    public List<String> getStrainIds(String reactorId) {
        return bioreactorStrains.getOrDefault(reactorId, List.of());
    }

    public List<StrainConfig> getAll() {
        return new ArrayList<>(strains.values());
    }

    public void delete(String strainId) {
        StrainConfig s = strains.remove(strainId);
        if (s != null) {
            List<String> ids = bioreactorStrains.get(s.getReactorId());
            if (ids != null) ids.remove(strainId);
        }
    }



}

package com.digitaltwin.envservice.service;

import com.digitaltwin.envservice.model.EnvState;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnvService {

    private final ConcurrentHashMap<String, Double> temps = new ConcurrentHashMap<>();

    public EnvState setRoomTemp(String roomId, double t) {
        temps.put(roomId, t);
        return new EnvState(roomId, t);
    }

    public EnvState getState(String roomId) {

        return new EnvState(roomId, temps.getOrDefault(roomId, 22.0));
    }

}

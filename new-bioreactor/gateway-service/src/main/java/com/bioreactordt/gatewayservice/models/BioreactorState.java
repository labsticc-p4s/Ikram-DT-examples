package com.bioreactordt.gatewayservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BioreactorState {

    private String  reactorId;
    private String experimentId;
    private Map<String, Double> sensorReadings;
}

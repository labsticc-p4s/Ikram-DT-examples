package com.bioreactordt.modelsservice.models;

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

    private String reactorId;
    private Map<String, Double> sensorReadings;


}

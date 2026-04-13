package com.bioreactordt.digitaltwinservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationStep {

    private double ph;
    private double temperature;
    private double realDurationHours;
    private List<String> strainIds;



}

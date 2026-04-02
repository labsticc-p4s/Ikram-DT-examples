package com.bioreactordt.digitaltwinservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class simulationStep {

    private double ph;
    private double temperature;
    private double realDurationHours;


}

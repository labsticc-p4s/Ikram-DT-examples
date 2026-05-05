package com.bioreactordt.experimentservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentationState {

    private double ph;
    private double temperature;
    private double population;
    private double mu;
    private double gammaPh;
    private double gammaTemp;
    private String growthStatus;
}

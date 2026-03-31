package com.bioreactordt.bioreactormockservice.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class bioreactorConfig {

    private double minPh;
    private double maxPh;
    private double optPh;

    private double minTemp;
    private double maxTemp;
    private double optTemp;

    private double populationInit;

    private double muFactor;

}

package com.bioreactordt.strainservice.models;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrainConfig {

    private String strainId;
    private String reactorId;
    private String name;

    private double muMax;

    private double populationInit;
    private double populationMax;
    private double latency;

    private double phMin;
    private double phOpt;
    private double phMax;

    private double tempMin;
    private double tempOpt;
    private double tempMax;
}

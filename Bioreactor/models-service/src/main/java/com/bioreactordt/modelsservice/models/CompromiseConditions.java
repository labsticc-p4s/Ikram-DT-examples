package com.bioreactordt.modelsservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompromiseConditions {

    private double       phMin;
    private double       phOpt;
    private double       phMax;

    private double       tempMin;
    private double       tempOpt;
    private double       tempMax;

    private double       muMax;

    private double       populationInit;
    private double       populationMax;

    private double       latency;
    private List<String> strainIds;
    private boolean      incompatible;
    private String       incompatibilityReason;
}

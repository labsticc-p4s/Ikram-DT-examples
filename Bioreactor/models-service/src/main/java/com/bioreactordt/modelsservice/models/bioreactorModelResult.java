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
public class BioreactorModelResult {

    private long   tupleId;
    private String reactorId;
    private String source;

    private double ph;
    private double temperature;
    private double population;
    private double elapsedHours;


    private double gammaPh;
    private double gammaTemp;
    private double mu;

    private String growthStatus;

    private List<String> strainIds;
}

package com.bioreactordt.shadowservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class bioreactorModelResult {

    private long   tupleId;
    private String reactorId;
    private String source;

    private double ph;
    private double temperature;
    private double population;

    private double gammaPh;
    private double gammaTemp;
    private double mu;

    private String growthStatus;
}

package com.bioreactordt.digitaltwinservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class bioreactorState {

    // facteurs environnementaux
    private String reactorId;

    private double ph;
    private double temperature;

    private double population;
    private double hours;


}

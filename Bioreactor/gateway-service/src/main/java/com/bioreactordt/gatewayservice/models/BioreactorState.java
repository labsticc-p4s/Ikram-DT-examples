package com.bioreactordt.gatewayservice.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BioreactorState { //this what it receives from the physical bioreactor

    private String reactorId;

    private double ph;
    private double temperature;

    private double population;
    private double hours;


}

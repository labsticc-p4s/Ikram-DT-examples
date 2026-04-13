package com.bioreactordt.gatewayservice.models;


import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedBioreactorState { //this is what gonna be sent to the model

    private String       reactorId;
    private List<String> strainIds;
    private double       ph;
    private double       temperature;
    private double       hours;

    private boolean      valid;
    private String       invalidReason;
}

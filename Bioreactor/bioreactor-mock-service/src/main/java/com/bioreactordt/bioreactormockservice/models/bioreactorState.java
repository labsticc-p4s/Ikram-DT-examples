package com.bioreactordt.bioreactormockservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class bioreactorState {

    private String reactorId;
    private double pH;
    private double temp;

    private double population;


}

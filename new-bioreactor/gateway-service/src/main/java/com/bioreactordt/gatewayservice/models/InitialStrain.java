package com.bioreactordt.gatewayservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitialStrain {

    private String condId;
    private double populationInit;
    private double populationMax;
    private List<String> familyIds;
}

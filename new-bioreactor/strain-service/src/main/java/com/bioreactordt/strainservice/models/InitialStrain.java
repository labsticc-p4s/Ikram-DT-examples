package com.bioreactordt.strainservice.models;

import lombok.*;

import java.util.List;
import java.util.Map;


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

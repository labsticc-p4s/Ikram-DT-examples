package com.bioreactordt.digitaltwinservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BioreactorState {

    private String       reactorId;
    private List<String> strainIds;
    private double       ph;
    private double       temperature;
    private double       hours;
    private String       source;



}

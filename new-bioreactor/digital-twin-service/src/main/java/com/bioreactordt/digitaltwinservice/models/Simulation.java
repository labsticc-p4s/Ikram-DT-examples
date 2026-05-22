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
public class Simulation {

    private String reactorId;
    private String condInit;

    private List<SimulationStep> steps;

    private int totalScreenMin;
    private int ticksPerStep;

    private String populationModel;
    private String phModel;
    private String tempModel;


}

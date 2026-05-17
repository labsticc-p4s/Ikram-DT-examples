package com.bioreactordt.digitaltwinservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationStep {

    private double ph;
    private double temperature;
    private double realDurationMin;
  //  private int tickPerStep;

}

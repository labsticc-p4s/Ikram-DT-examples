package com.digitaltwin.shadow.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelResult {

    private long tupleId;

    private String lampId;
    private String source;
    @JsonProperty("isOn")
    private boolean isOn;
    private double brightness;
    private double temperature;
    private double usageMinutes;
    private double roomTemp;

    private double powerWatts;
    private double energyConsumedWh;

    private String tempStatus;

    private double remainingLifespanMinutes;
    private String lifespanStatus;
}

package com.digitaltwin.datamanagerservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DataStateModel {

    private String  lampId;
    private String  source;
    @JsonProperty("isOn")
    private boolean isOn;
    private double  brightness;
    private double  usageMinutes;

    private double  roomTemp;
    private double  temperature;

    private boolean hasTemperatureSensor;
    private double  maxWatts;
    private double  maxLifeMinutes;
}

package com.digitaltwin.models.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DataStateModel {

    private String  lampId;
    private String  source;
    @JsonProperty("isOn")
    private boolean isOn;
    private double  brightness;
    private double  usageMinutes;

    private double  roomTemp;
    private double  temperature;

    // lamp config — from registry
    private boolean hasTemperatureSensor;
    private double  maxWatts;
    private double  maxLifeMinutes;



}

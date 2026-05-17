package com.digitaltwin.physical.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LampState {

    private String lampId;
    @JsonProperty("isOn")
    private boolean isOn;
    private double brightness;
    private double temperature;
    private double usageMinutes;
    private double roomTemp;

}

package com.digitaltwin.physical.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanpConfig {

    private String  lampId;
    private String  host;
    private int     port;
    private String  description;
    private String room;

    private boolean hasTemperatureSensor;
    private boolean hasRoomTempSensor;

    private double  maxWatts;
    private double  maxLifeMinutes;
}

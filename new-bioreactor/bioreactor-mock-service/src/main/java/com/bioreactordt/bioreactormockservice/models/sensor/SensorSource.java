package com.bioreactordt.bioreactormockservice.models.sensor;

import java.util.Map;

public interface SensorSource {
    Map<String, Double> read();
}

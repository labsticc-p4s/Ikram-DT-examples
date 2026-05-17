package com.digitaltwin.twin.model;

public class SimulationStep {

    public double brightness;
    public double roomTemp;
    public double realDurationHours;

    public SimulationStep(double b, double rt, double rdh) {
        this.brightness        = b;
        this.roomTemp          = rt;
        this.realDurationHours = rdh;
    }
}

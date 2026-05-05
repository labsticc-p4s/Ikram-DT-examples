package com.bioreactordt.modelsservice.calculators.temperature;

public interface TempGammaFunction {
    double calculate(double temp, double tempMin, double tempOpt, double tempMax);

}

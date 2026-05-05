package com.bioreactordt.modelsservice.calculators.temperature;

public class CardinalTempGamma implements TempGammaFunction{
    @Override
    public double calculate(double temp, double tempMin, double tempOpt, double tempMax) {
        if (temp <= tempMin || temp >= tempMax) return 0.0;
        double bast = (temp - tempMax) * Math.pow(temp - tempMin, 2);
        double maqem = (tempOpt - tempMin) * ((tempOpt - tempMin) * (temp - tempOpt) - (tempOpt - tempMax) * (tempOpt + tempMin - 2 * temp));
        return maqem == 0 ? 0.0 : bast / maqem;
    }
}


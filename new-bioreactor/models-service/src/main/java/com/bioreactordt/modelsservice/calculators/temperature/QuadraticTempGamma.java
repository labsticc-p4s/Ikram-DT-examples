package com.bioreactordt.modelsservice.calculators.temperature;

public class QuadraticTempGamma implements TempGammaFunction{
    @Override
    public double calculate(double temp, double tempMin, double tempOpt, double tempMax) {
        if (temp <= tempMin || temp >= tempMax) return 0.0;

        double bast = (temp - tempMin) * (tempMax - temp);
        double maqem = (tempOpt - tempMin) * (tempMax - tempOpt);

        return maqem == 0 ? 0.0 : bast / maqem;
    }
}

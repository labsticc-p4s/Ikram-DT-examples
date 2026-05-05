package com.bioreactordt.modelsservice.calculators.pH;

public class CardinalPhGamma implements PhGammaFunction{

    @Override
    public double calculate(double ph, double phMin, double phOpt, double phMax) {
        if (ph <= phMin || ph >= phMax) return 0.0;
        double bast = (ph - phMax) * Math.pow(ph - phMin, 1);
        double maqem =  ((phOpt - phMin) * (ph - phOpt) - (phOpt - phMax) * (phMin - ph));
        return maqem == 0 ? 0.0 : bast / maqem;
    }
}



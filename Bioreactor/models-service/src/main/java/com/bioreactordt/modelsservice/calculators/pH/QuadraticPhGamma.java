package com.bioreactordt.modelsservice.calculators.pH;

public class QuadraticPhGamma implements PhGammaFunction{

    @Override
    public double calculate(double ph, double phMin, double phOpt, double phMax) {
        if (ph <= phMin || ph >= phMax) return 0.0;
        double bast   = (ph - phMin) * (phMax - ph);
        double maqem = (phOpt - phMin) * (phMax - phOpt);
        return maqem == 0 ? 0.0 : bast / maqem;
    }
}

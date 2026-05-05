package com.bioreactordt.modelsservice.calculators.pH;

public interface PhGammaFunction {

    double calculate(double ph, double phMin, double phOpt, double phMax);

}

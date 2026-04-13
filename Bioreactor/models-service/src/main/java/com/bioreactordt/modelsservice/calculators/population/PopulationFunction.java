package com.bioreactordt.modelsservice.calculators.population;

public interface PopulationFunction {

    PopulationResult calculate(String reactorId, double deltaHours,
                               double mu, double N0, double Nmax, double latency);

    record PopulationResult(double population, double elapsedHours, String phase) {}

    void reset(String reactorId);

}

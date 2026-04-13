package com.bioreactordt.modelsservice.calculators.population;

import java.util.concurrent.ConcurrentHashMap;

public class LogisticPopulation implements PopulationFunction{

    private final ConcurrentHashMap<String, Double> elapsedHoursMap = new ConcurrentHashMap<>();

    @Override
    public PopulationResult calculate(String reactorId, double deltaHours, double mu, double N0, double Nmax, double latency) {
        double t = elapsedHoursMap.merge(reactorId, deltaHours, Double::sum);

        double Nt;
        if (t <= latency) {
            Nt = N0;
        } else {
            double exponent =
                    -Math.exp((mu * Math.E / Nmax) * (latency - t) + 1);

            Nt = Nmax * Math.exp(exponent);
        }

        return new PopulationResult(Math.round(Nt), t, resolvePhase(t, latency, Nt, Nmax));
    }

    @Override
    public void reset(String reactorId) {
        elapsedHoursMap.remove(reactorId);
    }

    private String resolvePhase(double t, double latency, double Nt, double Nmax) {
        if (t <= latency)  return "LAG";
        if (Nt < Nmax * 0.95)  return "ACCELERATION";
        return "STATIONARY";
    }

}

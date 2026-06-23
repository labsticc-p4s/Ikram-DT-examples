package com.bioreactordt.modelsservice.services;

import com.bioreactordt.shared.GreycatClient;
import com.bioreactordt.modelsservice.calculators.pH.*;
import com.bioreactordt.modelsservice.calculators.population.*;
import com.bioreactordt.modelsservice.calculators.temperature.*;
import com.bioreactordt.modelsservice.kafka.ExperimentStateProducer;
import com.bioreactordt.modelsservice.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModelService {

    private final GreycatClient greycat;
    private final ExperimentStateProducer producer;


    private final ConcurrentHashMap<String, PopulationFunction> popFunctions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> contextCache = new ConcurrentHashMap<>();



    public void onBioreactorState(BioreactorState state) {
        String id = state.getExperimentId();

        Map<String, Object> ctx = contextCache.get(id);
        if (ctx == null) {
            try {
                ctx = greycat.callMap("project::get_experiment_for_compute", id);
            } catch (Exception e) {
                log.debug("experiment not ready in GreyCat yet ({}): {}", id, e.getMessage());
                return;
            }
            if (ctx == null) {
                log.debug("experiment not found in GreyCat{}", id);
                return;
            }
            contextCache.put(id, ctx);
        }

        double ph   = state.getSensorReadings().getOrDefault("ph", 7.0);
        double temp = state.getSensorReadings().getOrDefault("temperature", 37.0);

        String popModel = (String) ctx.get("populationModel");
        String phModel  = (String) ctx.get("phModel");
        String tmpModel = (String) ctx.get("tempModel");
        double muMax    = d(ctx.get("muMax"));
        double latency  = d(ctx.get("latency"));
        double phMin    = d(ctx.get("phMin")),   phOpt  = d(ctx.get("phOpt")),   phMax  = d(ctx.get("phMax"));
        double tempMin  = d(ctx.get("tempMin")),  tempOpt = d(ctx.get("tempOpt")), tempMax = d(ctx.get("tempMax"));
        double popInit  = d(ctx.get("populationInit"));
        double popMax   = d(ctx.get("populationMax"));

        PopulationFunction popFn = popFunctions.computeIfAbsent(id, k -> resolvePopulation(popModel));

        double gammaPh   = resolvePhGamma(phModel).calculate(ph, phMin, phOpt, phMax);
        double gammaTemp = resolveTempGamma(tmpModel).calculate(temp, tempMin, tempOpt, tempMax);
        double mu        = muMax * gammaPh * gammaTemp;

        PopulationFunction.PopulationResult pop =
                popFn.calculate(id, 1.0 / 3600.0, mu, popInit, popMax, latency);

        producer.send(id, ExperimentationState.builder()
                .experimentId(id)
                .ph(ph).temperature(temp)
                .population(pop.population()).mu(mu)
                .gammaPh(gammaPh).gammaTemp(gammaTemp)
                .growthStatus(pop.phase())
                .build());
    }




    private double d(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }




    private PhGammaFunction resolvePhGamma(String model) {
        return switch (model.toLowerCase()) {
            case "cardinal"  -> new CardinalPhGamma();
            case "quadratic" -> new QuadraticPhGamma();
            default -> throw new IllegalArgumentException("Unknown pH model: " + model);
        };
    }

    private TempGammaFunction resolveTempGamma(String model) {
        return switch (model.toLowerCase()) {
            case "cardinal"  -> new CardinalTempGamma();
            case "quadratic" -> new QuadraticTempGamma();
            default -> throw new IllegalArgumentException("Unknown temp model: " + model);
        };
    }

    private PopulationFunction resolvePopulation(String model) {
        return switch (model.toLowerCase()) {
            case "logistic" -> new LogisticPopulation();
            case "gompertz" -> new GompertzPopulation();
            default -> throw new IllegalArgumentException("Unknown population model: " + model);
        };
    }


}
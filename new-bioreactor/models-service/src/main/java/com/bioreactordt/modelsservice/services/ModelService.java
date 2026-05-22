package com.bioreactordt.modelsservice.services;


import com.bioreactordt.modelsservice.calculators.pH.*;
import com.bioreactordt.modelsservice.calculators.population.*;
import com.bioreactordt.modelsservice.calculators.temperature.*;
import com.bioreactordt.modelsservice.kafka.ExperimentStateProducer;
import com.bioreactordt.modelsservice.models.*;
import greycat.GreyCat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ModelService {

    private  GreyCat greycat;
    private final String greycatUrl;
    private  ExperimentStateProducer producer;


    //active experiment per exp id
    private final ConcurrentHashMap<String, Experimentation> experiments = new ConcurrentHashMap<>();

    //latest bioreactor readings per reactorId
    private final ConcurrentHashMap<String, BioreactorState> latestStates = new ConcurrentHashMap<>();

    //cache strains
    private final ConcurrentHashMap<String, double[]> strainParamsCache = new ConcurrentHashMap<>();

    public ModelService(@Value("${greycat.url}") String greycatUrl, ExperimentStateProducer producer) {
        this.greycatUrl = greycatUrl;
        this.producer = producer;
    }


    private synchronized GreyCat greycat() {
        if (greycat == null) {
            try {
                greycat = new GreyCat(greycatUrl, null, false, false);
                log.info("Connected to GreyCat at {}", greycatUrl);
            } catch (Exception e) {
                log.error("Failed to connect to GreyCat at {}: {}", greycatUrl, e.getMessage());
                throw new RuntimeException("Failed to connect to GreyCat at " + greycatUrl, e);
            }
        }
        return greycat;
    }


    //when received the experimentation from the experiment-service
    public void onExperimentStarted(Experimentation exp) {
        experiments.putIfAbsent(exp.getExperimentId(), exp);
        //log.info("Experiment registered for reactor {}", exp.getReactorId());
    }

    //when recieved bioreactor state from the gateway after normalization
    public void onBioreactorState(BioreactorState state) throws IOException {


        Experimentation exp = experiments.get(state.getExperimentId());
        if (exp == null){
            log.debug("No active experiment for experimentId={}", state.getExperimentId());
            return;
        }
        log.info("ikram loves islam {}", exp);
        ExperimentationState result = compute(state, exp);
        producer.send(exp.getExperimentId(), result);



    }




    private ExperimentationState compute(BioreactorState state, Experimentation exp) throws IOException {

        double ph = state.getSensorReadings().getOrDefault("ph", 7.0);
        double temp = state.getSensorReadings().getOrDefault("temperature", 37.0);

        Object raw = greycat().call("project::get_strain_params", exp.getCondInit());
        String str = raw.toString()
                .replace("core::Array[", "")
                .replace("]", "");
        String[] p = str.split(",");


        double muMax   = Double.parseDouble(p[0]);
        double latency = Double.parseDouble(p[1]);
        double phMin   = Double.parseDouble(p[2]);
        double phOpt   = Double.parseDouble(p[3]);
        double phMax   = Double.parseDouble(p[4]);
        double tempMin = Double.parseDouble(p[5]);
        double tempOpt = Double.parseDouble(p[6]);
        double tempMax = Double.parseDouble(p[7]);
        double popInit = Double.parseDouble(p[8]);
        double popMax  = Double.parseDouble(p[9]);

        PhGammaFunction   phGamma   = resolvePhGamma(exp.getPhModel());
        TempGammaFunction tempGamma = resolveTempGamma(exp.getTempModel());
        PopulationFunction popFn    = resolvePopulation(exp.getPopulationModel());

        double gammaPh   = phGamma.calculate(ph, phMin, phOpt, phMax);
        double gammaTemp = tempGamma.calculate(temp, tempMin, tempOpt, tempMax);
        double mu        = muMax * gammaPh * gammaTemp;

        PopulationFunction.PopulationResult popResult =
                popFn.calculate(exp.getReactorId(), 1.0 / 3600.0, mu, popInit, popMax, latency);

        log.info("here experimentation state received");
        return ExperimentationState.builder()
                .experimentId(exp.getExperimentId())
                .ph(ph)
                .temperature(temp)
                .population(popResult.population())
                .mu(mu)
                .gammaPh(gammaPh)
                .gammaTemp(gammaTemp)
                .growthStatus(popResult.phase())
                .build();
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

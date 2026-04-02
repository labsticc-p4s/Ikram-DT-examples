package com.bioreactordt.modelsservice.services;

import com.bioreactordt.modelsservice.kafka.bioreactorModelResultProducer;
import com.bioreactordt.modelsservice.models.bioreactorConfig;
import com.bioreactordt.modelsservice.models.bioreactorModelResult;
import com.bioreactordt.modelsservice.models.bioreactorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class bioreactorModelService {


    private final bioreactorModelResultProducer producer;
    private final RestTemplate restTemplate;

    private final ConcurrentHashMap<String, Double> elapsedHours  = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, bioreactorConfig> configCache = new ConcurrentHashMap<>();

    private static final String PHYSICAL_URL = "http://physical-service:8081";


    public void compute(bioreactorState s, String source) {
        bioreactorConfig c = "SIMULATION".equals(source) ? defaultConfig(s.getReactorId()) : getConfig(s.getReactorId());


        //1- calculate gamma for ph and temp
        double gammaPH   = gamma(s.getPh(), c.getMinPh(),   c.getOptPh(),   c.getMaxPh(), 1);
        double gammaTemp = gamma(s.getTemperature(), c.getMinTemp(), c.getOptTemp(), c.getMaxTemp(), 2);

       //2- calculate la vitesse opt de croissance
        double mu = c.getMuFactor() * gammaPH * gammaTemp;

        //3- calculate population growth
        double dt = s.getHours();
        double t  = elapsedHours.merge(s.getReactorId(), dt, Double::sum);


        double lat = c.getLatency();
        double N0 = c.getPopulationInit();
        double Nmax = c.getPopulationMax();

        double Nt;
        if(t <= lat) Nt = N0;
        else {
            double lnNt = Math.log(Nmax) - Math.log(1.0 + (Nmax / N0 - 1.0) * Math.exp(-mu * (t - lat)));
            Nt = Math.exp(lnNt);
        }


        //4- predict growth
        String status;
        double ratio = Nt / Nmax;

        log.info(" t={} lat={} Nt={} ratio={}",

                elapsedHours.getOrDefault(s.getReactorId(), 0.0),
                c.getLatency(), Nt, ratio);

        if (t <= lat) {
            status = "LAG";
        } else if (ratio < 0.01) {
            status = "ACCELERATION";
        } else if (ratio < 0.50) {
            status = "EXPONENTIAL";
        } else if (ratio < 0.95) {
            status = "DECELERATION";
        } else {
            status = "STATIONARY";
        }

        producer.send(bioreactorModelResult.builder()
                .reactorId(s.getReactorId())
                .source(source)
                .ph(s.getPh())
                .temperature(s.getTemperature())
                .population(Math.round(Nt))
                .gammaPh(round2(gammaPH))
                .gammaTemp(round2(gammaTemp))
                .mu(round4(mu))
                .growthStatus(status)
                .build());
    }

    //generic formula
    private double gamma(double x, double min, double opt, double max, int n) {
        if (x <= min || x >= max) return 0.0;
        double bast   = (x - max) * Math.pow(x - min, n);
        double maqem = Math.pow(opt - min, n-1) * ((opt - min) * (x - opt) - (opt - max) * ((n-1) * opt + min - n * x));
        return maqem == 0 ? 0.0 : bast / maqem;
    }


    private double gammaPh(double x, double min, double opt, double max) {
        if (x <= min || x >= max) return 0.0;
        double bast   = (x - min) * (max - x);
        double maqem = (opt - min) * (max - opt);
        return maqem == 0 ? 0.0 : bast / maqem;
    }

    private double gammaTemp(double x, double min, double opt, double max) {
        if (x <= min || x >= max) return 0.0;
        double bast   = (x - min) ;
        double maqem = (opt - min) ;
        return maqem == 0 ? 0.0 : Math.pow(bast / maqem, 2);
    }

    private bioreactorConfig getConfig(String reactorId) {
        bioreactorConfig cached = configCache.get(reactorId);
        if (cached != null) return cached;

        try {
            bioreactorConfig conf = restTemplate.getForObject(
                    PHYSICAL_URL + "/api/physical/config/" + reactorId,
                    bioreactorConfig.class);
            if (conf != null) {
                configCache.put(reactorId, conf);
                return conf;
            }
        } catch (Exception e) {
            log.warn("Could not get bioreactor config" );
        }
        return defaultConfig(reactorId);
    }

    //for sum
    private bioreactorConfig defaultConfig(String id) {
        return bioreactorConfig.builder()
                .reactorId(id)
                .populationInit(1_000_000.0)
                .muFactor(0.5)
                .minPh(5.0).maxPh(9.0).optPh(7.0)
                .minTemp(25.0).maxTemp(45.0).optTemp(37.0)
                .populationMax(1.0E10)
                .latency(0.01)
                .build();
    }

    public bioreactorConfig getConfig_public(String reactorId) {
        return getConfig(reactorId);
    }

    private double round2(double v) { return Math.round(v * 100.0)   / 100.0; }
    private double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }



}

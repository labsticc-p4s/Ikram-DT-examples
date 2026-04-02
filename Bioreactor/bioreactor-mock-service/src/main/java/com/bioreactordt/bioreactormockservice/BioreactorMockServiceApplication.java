package com.bioreactordt.bioreactormockservice;

import com.bioreactordt.bioreactormockservice.models.bioreactorConfig;
import com.bioreactordt.bioreactormockservice.services.bioreactorConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class BioreactorMockServiceApplication implements CommandLineRunner {


    private final bioreactorConfigService service;


    @Value("${reactor.id}")                       private String reactorId;
    @Value("${reactor.config.mu-max}")            private double muMax;
    @Value("${reactor.config.population-init}")   private double populationInit;
    @Value("${reactor.config.ph.min}")            private double phMin;
    @Value("${reactor.config.ph.opt}")            private double phOpt;
    @Value("${reactor.config.ph.max}")            private double phMax;
    @Value("${reactor.config.temp.min}")          private double tempMin;
    @Value("${reactor.config.temp.opt}")          private double tempOpt;
    @Value("${reactor.config.temp.max}")          private double tempMax;
    @Value("${reactor.config.population-max}")    private double populationMax;
    @Value("${reactor.config.latency}")          private double latency;




    public static void main(String[] args) {
        SpringApplication.run(BioreactorMockServiceApplication.class, args);
    }


    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }


    @Override
    public void run(String... args) throws Exception {
        service.addConf(bioreactorConfig.builder()
                .reactorId(reactorId)
                .populationInit(populationInit)
                .muFactor(muMax)
                .minPh(phMin).maxPh(phMax).optPh(phOpt)
                .minTemp(tempMin).maxTemp(tempMax).optTemp(tempOpt)
                .populationMax(populationMax)
                .latency(latency)
                .build()
        );

    }
}

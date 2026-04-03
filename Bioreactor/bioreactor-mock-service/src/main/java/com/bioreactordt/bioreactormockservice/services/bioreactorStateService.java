package com.bioreactordt.bioreactormockservice.services;

import com.bioreactordt.bioreactormockservice.kafka.bioreactorProducer;
import com.bioreactordt.bioreactormockservice.models.bioreactorState;
import com.bioreactordt.bioreactormockservice.replay.RawDataSheet;
import com.bioreactordt.bioreactormockservice.replay.TimeSerie;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class bioreactorStateService {

    private final bioreactorProducer producer;

    @Value("${reactor.id}")
    private String reactorId;

    @Value("${reactor.config.population-init}")
    private double populationInit;

    @Value("${reactor.csv.path}")
    private String csvPath;

    @Value("${reactor.csv.speed-factor:30}")
    private long speedFactorInit;

    private static final String COL_TEMP = "1TC01 - Temperature_MV";
    private static final String COL_PH   = "1AC04 - pH_MV";

    private TimeSerie<String, String> timeSerie;
    private volatile long   speedFactor   = 30;
    private          long   elapsedSeconds = 0;
    private          double ph             = 7.0;
    private          double temperature    = 37.0;
    private          double population;

    @PostConstruct
    public void init() {
        population  = populationInit;
        speedFactor = speedFactorInit;
        try {
            RawDataSheet sheet = new RawDataSheet(
                    csvPath,
                    ";",
                    line -> line.toLowerCase().contains("date")
            );
            timeSerie = sheet.toTimeSerie(0, RawDataSheet::smartFermentTimeParser);
            log.info("CSV loaded — {} rows ready for replay at {}x speed",
                    sheet.getEntries().length, speedFactor);
        } catch (Exception e) {
            log.error("Could not load CSV '{}' — using default ph/temp. Error: {}",
                    csvPath, e.getMessage());
            timeSerie = null;
        }
    }

    @Scheduled(fixedRate = 1000)
    public void update() {
        if (timeSerie != null) {
            try {
                Duration d  = Duration.ofSeconds(elapsedSeconds);
                temperature = helper(timeSerie.getValue(COL_TEMP, d));
                ph   = helper(timeSerie.getValue(COL_PH,   d));
            } catch (Exception e) {
                log.info("End of CSV data — looping back to start");
                elapsedSeconds = 0;
            }
            elapsedSeconds += speedFactor;
        }
        producer.send(buildState());
    }

    public bioreactorState buildState() {
        return bioreactorState.builder()
                .reactorId(reactorId)
                .ph(Math.round(ph * 100.0) / 100.0)
                .temperature(Math.round(temperature * 100.0) / 100.0)
                .population(Math.round(population))
                .hours(Math.round((elapsedSeconds / 3600.0) * 100.0) / 100.0)
                .build();
    }

    public bioreactorState getState()  {
        return buildState();
    }
    public void setSpeedFactor(long factor){
        this.speedFactor = Math.max(1, factor);
    }
    public long getSpeedFactor() {
        return speedFactor;
    }

    private double helper(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try {
            return Double.parseDouble(s.trim().replace(",", "."));
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
package com.bioreactordt.bioreactormockservice.services;

import com.bioreactordt.bioreactormockservice.kafka.BioreactorProducer;
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

    private final BioreactorProducer producer;

    @Value("${reactor.id}")
    private String reactorId;

    @Value("${reactor.csv.path}")
    private String csvPath;

    @Value("${reactor.csv.speed-factor:30}")
    private long speedFactorInit;

    private static final String COL_TEMP = "1TC01 - Temperature_MV";
    private static final String COL_PH   = "1AC04 - pH_MV";

    private TimeSerie<String, String> timeSerie;
    private long   elapsedSeconds = 0;
    private double ph = 7.0;
    private double temperature= 37.0;

    @PostConstruct
    public void init() {
        try {
            RawDataSheet sheet = new RawDataSheet(
                    csvPath,
                    ";",
                    line -> line.toLowerCase().contains("date")
            );
            timeSerie = sheet.toTimeSerie(0, RawDataSheet::smartFermentTimeParser);
            log.info("CSV loaded — {} rows ready for replay at {}x speed",
                    sheet.getEntries().length, speedFactorInit);
        } catch (Exception e) {
            log.error(" Error: {}", e.getMessage());
            timeSerie = null;
        }
    }

    @Scheduled(fixedRate = 1000) //each 1 second
    public void update() {
        if (timeSerie != null) {
            elapsedSeconds += speedFactorInit; //add with speed factor which is 30 second
            try {
                Duration d  = Duration.ofSeconds(elapsedSeconds);
                temperature = helper(timeSerie.getValue(COL_TEMP, d));
                ph   = helper(timeSerie.getValue(COL_PH,   d));
            } catch (Exception e) {
                elapsedSeconds = 0;
            }
        }
        producer.send(buildState());
    }

    public bioreactorState buildState() {
        return bioreactorState.builder()
                .reactorId(reactorId)
                .ph(Math.round(ph * 100.0) / 100.0)
                .temperature(Math.round(temperature * 100.0) / 100.0)
                .hours(Math.round((elapsedSeconds / 60.0) * 100.0) / 100.0)
                .build();
    }

    public bioreactorState getState()  {
        return buildState();
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
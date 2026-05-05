package com.bioreactordt.bioreactormockservice.models.sensor;

import com.bioreactordt.bioreactormockservice.replay.RawDataSheet;
import com.bioreactordt.bioreactormockservice.replay.TimeSerie;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class CsvSensorBioreactorData implements SensorSource{

    private final TimeSerie<String, String> timeSerie;
    private final Map<String, String>   sensorToColumn;
    private long   elapsedSeconds = 0;


    public CsvSensorBioreactorData(String csvPath, Map<String, String> sensorToColumn) {
        this.sensorToColumn = sensorToColumn;
        try {
            RawDataSheet sheet = new RawDataSheet(csvPath, ";", line -> line.toLowerCase().contains("date"));
            this.timeSerie = sheet.toTimeSerie(0, RawDataSheet::smartFermentTimeParser);
            log.info("CSV loaded: {} rows at {}x speed",
                    sheet.getEntries().length, 30);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load CSV: " + csvPath, e);
        }
    }

    @Override
    public Map<String, Double> read() {
        elapsedSeconds += 30;
        Map<String, Double> values = new LinkedHashMap<>();
        try {
            Duration d = Duration.ofSeconds(elapsedSeconds);
            for (var entry : sensorToColumn.entrySet()) {
                String raw = timeSerie.getValue(entry.getValue(), d);
                values.put(entry.getKey(), parseDouble(raw));
            }
        } catch (Exception e) {
            log.info("End of CSV — looping back");
            elapsedSeconds = 0;
        }
        return values;
    }

    public long getElapsedSeconds() { return elapsedSeconds; }

    private double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (NumberFormatException e) { return 0.0; }
    }


}

package com.bioreactordt.bioreactormockservice.replay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RawDataSheet {
    private final String[] headers;
    private final String[][] entries;
    private final Map<String, Integer> headerIndexMap = new HashMap<>();

    public RawDataSheet(String fileName, String separator, Predicate<String> headerPredicate) throws IOException {

        Iterator<String> lines = Files.readAllLines(Paths.get(fileName)).iterator();
        String currentLine = lines.next();
        while (!headerPredicate.test(currentLine)) {currentLine = lines.next();}
        this.headers = currentLine.split(";");
        for (int i = 0; i < this.headers.length; i++) {
            headerIndexMap.put(this.headers[i], i);
        }
        List<String[]> entryList = new ArrayList<>();
        while (lines.hasNext()) {
            currentLine = lines.next();
            String[] currentEntry = currentLine.split(separator);
            String[] normalizedEntry = Arrays.copyOf(currentEntry, this.headers.length);
            entryList.add(normalizedEntry);
        }
        this.entries = new String[entryList.size()][headers.length];
        for (int i = 0 ; i < this.entries.length; i++) {
            String[] currentEntry = entryList.get(i);
            System.arraycopy(currentEntry, 0, this.entries[i], 0, this.headers.length);
        }
    }
    public String[] getHeaders() {
        return this.headers;
    }

    public String[][] getEntries() {
        return this.entries;
    }

    public TimeSerie<String, String> toTimeSerie(int timeIndex, Function<String, LocalDateTime> timeParser) {
        MapTimeSerie<String, String> result = new MapTimeSerie<>();
        LocalDateTime startDate = null;
        for (String header : this.headers) {
            result.addId(header);
        }
        for (String[] entry : this.entries) {
            LocalDateTime currentDate = timeParser.apply(entry[timeIndex]);
            if (startDate == null) {startDate = currentDate;}
            Duration elapsedTime = Duration.between(startDate, currentDate);
            for (int i = 0; i < this.headers.length; i++) {
                result.addDataPoint(this.headers[i], elapsedTime, entry[i]);
            }
        }
        return result;
    }

    public static LocalDateTime smartFermentTimeParser(String dateString) {
        String[] components = dateString.split(" ");
        String[] dayComponents = components[0].split("/");
        return LocalDateTime.parse(dayComponents[2] + "-" + dayComponents[1] + "-" + dayComponents[0] + "T" + components[1] +".000");
    }
    public static void main(String[] args) throws IOException {
        String fileName = "bioreactor-mock-service/src/main/resources/demo.csv";
        String[] headers = { "1TC01 - Temperature_", "1AC04 - pH_" };
        int[] timeStamps = {30, 60, 3600, 7200};
        Predicate<String> headerPredicate = (s) -> s.toLowerCase().contains("date");
        RawDataSheet rawDataSheet = new RawDataSheet(fileName, ";", headerPredicate);
        System.out.println("Actual values (sensors) : " + Arrays.stream(rawDataSheet.headers).sequential().filter(s -> s.contains("OUT")).collect(Collectors.toUnmodifiableList()));
        System.out.println("Set point values (control) : " + Arrays.stream(rawDataSheet.headers).sequential().filter(s -> s.contains("SP")).collect(Collectors.toUnmodifiableList()));
        // I don't remember what MV stands for
        TimeSerie<String, String> timeSerie = rawDataSheet.toTimeSerie(0, RawDataSheet::smartFermentTimeParser);
        TimeSerie<String, String> moddedTimeSerie = new ModTimeSerie<>(timeSerie, (d) -> d.multipliedBy(2));
        for (String header : headers) {
            for (int timeStamp : timeStamps) {
                System.out.println("timeSerie " + header + "MV " + timeStamp + "s : " + timeSerie.getValue(header + "MV", Duration.ofSeconds(timeStamp)));
                System.out.println("timeSerie " + header + "SP " + timeStamp + "s : " + timeSerie.getValue(header + "SP", Duration.ofSeconds(timeStamp)));
                System.out.println("timeSerie " + header + "OUT " + timeStamp + "s : " + timeSerie.getValue(header + "OUT", Duration.ofSeconds(timeStamp)));
                System.out.println("moddedTimeSerie " + header + "MV " + timeStamp + "s : " + moddedTimeSerie.getValue(header + "MV", Duration.ofSeconds(timeStamp)));
                System.out.println("moddedTimeSerie " + header + "SP " + timeStamp + "s : " + moddedTimeSerie.getValue(header + "SP", Duration.ofSeconds(timeStamp)));
                System.out.println("moddedTimeSerie " + header + "OUT " + timeStamp + "s : " + moddedTimeSerie.getValue(header + "OUT", Duration.ofSeconds(timeStamp)));
            }

        }
    }
}

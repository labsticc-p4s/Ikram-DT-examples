package com.bioreactordt.bioreactormockservice.services;

import com.bioreactordt.bioreactormockservice.kafka.BioreactorProducer;
import com.bioreactordt.bioreactormockservice.models.BioreactorState;
import com.bioreactordt.bioreactormockservice.models.sensor.CsvSensorBioreactorData;
import com.bioreactordt.bioreactormockservice.models.sensor.SensorSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BioreactorStateSendService {

    private final BioreactorProducer producer;
    private final SensorSource sensor;

    @Value("${reactor.id}") private String reactorId;

    public BioreactorStateSendService(
            BioreactorProducer producer,
            @Value("${reactor.source.csv.path}")  String csvPath,
            @Value("#{${reactor.source.csv.columns}}") Map<String, String> columns) {

        this.producer = producer;
        this.sensor = new CsvSensorBioreactorData(csvPath, columns);
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        Map<String, Double> readings = sensor.read();
        producer.send(BioreactorState.builder()
                .reactorId(reactorId)
                .sensorReadings(readings)
                .build());
    }

    public BioreactorState currentState() {
        return BioreactorState.builder()
                .reactorId(reactorId)
                .sensorReadings(sensor.read())
                .build();
    }


}

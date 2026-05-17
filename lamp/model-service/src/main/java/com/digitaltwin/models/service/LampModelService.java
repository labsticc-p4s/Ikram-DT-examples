package com.digitaltwin.models.service;

import com.digitaltwin.models.kafka.ModelProducer;
import com.digitaltwin.models.model.DataStateModel;
import com.digitaltwin.models.model.ModelResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service @RequiredArgsConstructor @Slf4j
public class LampModelService {

    private final ModelProducer producer;

    private final ConcurrentHashMap<String, Double>     totalEnergyWh  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double>     lastTemp       = new ConcurrentHashMap<>();


    public void compute(DataStateModel s) {
        String     id  = s.getLampId();

        //brightness model
        double  brightness  = s.getBrightness();

        //room temp model
        double roomTemp = s.getRoomTemp() >= 0 ? s.getRoomTemp() : 22.0; //from physical service, if no sensor of it, it sends -1


        // temp model
        double temp;
        if (s.getTemperature() >= 0) {
            temp = s.getTemperature();
            lastTemp.put(id, temp);
        } else {
            double previous = lastTemp.getOrDefault(id, roomTemp);
            double target   = s.isOn() ? roomTemp + (brightness / 100.0) * 45.0 : roomTemp;
            temp = previous < target
                    ? Math.min(previous + 2.0 , target)
                    : Math.max(previous - 0.5, target);
            lastTemp.put(id, temp);
        }


        // energy model
        double power = s.isOn() ? (brightness / 100.0) * s.getMaxWatts() : 0;
        totalEnergyWh.merge(id, power / 3600.0, Double::sum);
        double energy = totalEnergyWh.get(id);

        //temp status model
        String tempStatus = temp >= 90 ? "CRITICAL" : temp >= 70 ? "WARNING" : "NORMAL";

        //lifespan mpdel
        double remaining = Math.max(0, s.getMaxLifeMinutes() - s.getUsageMinutes());
        double usagePct  = s.getUsageMinutes() / s.getMaxLifeMinutes() * 100.0;
        String lifespan  = usagePct >= 90 ? "REPLACE_SOON" : usagePct >= 70 ? "AGING" : "GOOD";

        producer.send(ModelResult.builder()
                .lampId(id).source(s.getSource())
                .isOn(s.isOn())
                .brightness(brightness)
                .roomTemp(roomTemp)
                .temperature(Math.round(temp * 10) / 10.0)
                .usageMinutes(s.getUsageMinutes())
                .powerWatts(Math.round(power * 10) / 10.0)
                .energyConsumedWh(Math.round(energy * 10000) / 10000.0)
                .tempStatus(tempStatus)
                .remainingLifespanMinutes(Math.round(remaining * 10) / 10.0)
                .lifespanStatus(lifespan)
                .build());
    }



}

package com.digitaltwin.twin.service;

import com.digitaltwin.twin.kafka.TwinStateProducer;
import com.digitaltwin.twin.model.LampState;
import com.digitaltwin.twin.model.ModelResult;
import com.digitaltwin.twin.model.Simulation;
import com.digitaltwin.twin.model.SimulationStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service @RequiredArgsConstructor @Slf4j
public class SimulationService {

    private final TwinStateProducer producer;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10); //ten prepared threads to be executed for simualtion
    private final Map<String, Simulation> sims = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    //start simulation thread
    public String start(Simulation sc) {
        String simId = "SIM-" + counter.incrementAndGet();
        Simulation sim = new Simulation(simId, sc.description, sc.stepIntervalMs, sc.simDurationSeconds, sc.steps);
        sim.job = scheduler.scheduleAtFixedRate(() -> update(sim), 0, sim.stepIntervalMs, TimeUnit.MILLISECONDS);
        sims.put(simId, sim);
        return simId;
    }

    //stop simulation thread
    public boolean stop(String id)   {
        Simulation s = sims.get(id);
        if (s==null) return false;
        s.state="STOPPED";
        cancel(id);
        return true;
    }

    public boolean pause(String id)  {
        Simulation s = sims.get(id);
        if (s==null) return false;
        s.pausedAt = System.currentTimeMillis();
        s.paused=true;
        s.state="PAUSED";
        return true;
    }

    public boolean resume(String id) {
        Simulation s = sims.get(id);
        if (s==null) return false;
        s.paused=false;
        s.state="RUNNING";
        s.startTime += System.currentTimeMillis()-s.pausedAt;
        return true;
    }

    public int stopAll() {
        int n=sims.size();
        new ArrayList<>(sims.keySet()).forEach(this::stop);
        return n;
    }


    public List<Simulation> getAll()   {
        return sims.values().stream().sorted(Comparator.comparing(s -> s.simId)).toList();
    }

    //return simulation model results
    public void onModelResult(ModelResult r) {
        Simulation s = sims.get(r.getLampId());
        if (s != null  && !s.paused) s.latestModelResult = r;
    }

    private void update(Simulation sim) {

        if (sim.paused) return;

        SimulationStep ss = sim.currentStep();
        boolean on = ss.brightness > 0;
        double target = on ? ss.roomTemp + 20 + (ss.brightness * 0.25) : ss.roomTemp; //for temp of the simulation case
        double step = on ? 2.0 : 0.5;
        if (sim.temp < target) sim.temp = Math.min(sim.temp + step, target);
        else sim.temp = Math.max(sim.temp - step, target);

        if (on) sim.usageMinutes += sim.realMinutesPerUpdate;

        // send data to the model to calculate the other values
        producer.sendSimulation(sim.simId, LampState.builder()
                .lampId(sim.simId).isOn(on)
                .brightness(ss.brightness)
                .temperature(Math.round(sim.temp * 10) / 10.0)
                .usageMinutes(Math.round(sim.usageMinutes * 100) / 100.0)
                .roomTemp(ss.roomTemp)
                .build());

        //chcek the completion of calling update() afte executing
        long tick = sim.updateCount.incrementAndGet();
        if (tick >= sim.totalUpdates) {
            sim.state = "COMPLETED";
            if (sim.job != null) sim.job.cancel(false);
            scheduler.schedule(() -> sims.remove(sim.simId), 5, TimeUnit.SECONDS);        }
    }

    private void cancel(String id) {
        Simulation s = sims.remove(id);
        if (s!=null && s.job!=null) s.job.cancel(false);
    }
}

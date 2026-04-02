package com.bioreactordt.digitaltwinservice.services;

import com.bioreactordt.digitaltwinservice.kafka.twinStateProducer;
import com.bioreactordt.digitaltwinservice.models.bioreactorModelResult;
import com.bioreactordt.digitaltwinservice.models.bioreactorState;
import com.bioreactordt.digitaltwinservice.models.simulation;
import com.bioreactordt.digitaltwinservice.models.simulationStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class simulationService {

    private final twinStateProducer producer;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, simulation> sims = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    public String start(simulation sc) {
        String simId = "SIM-" + counter.incrementAndGet();
        simulation sim = new simulation(simId, sc.description,
                sc.stepIntervalMs, sc.simDurationSeconds, sc.steps);
        sim.state = "RUNNING";
        sim.job = scheduler.scheduleAtFixedRate(() -> update(sim), 0, sim.stepIntervalMs, TimeUnit.MILLISECONDS);
        sims.put(simId, sim);
        return simId;
    }

    public boolean stop(String id) {
        simulation s = sims.get(id);
        if (s == null) return false;
        s.state = "STOPPED";
        cancel(id);
        return true;
    }

    public boolean pause(String id) {
        simulation s = sims.get(id);
        if (s == null) return false;
        s.pausedAt = System.currentTimeMillis();
        s.paused = true;
        s.state  = "PAUSED";
        return true;
    }

    public boolean resume(String id) {
        simulation s = sims.get(id);
        if (s == null) return false;
        s.paused = false;
        s.state  = "RUNNING";
        s.startTime += System.currentTimeMillis() - s.pausedAt;
        return true;
    }

    public int stopAll() {
        int n = sims.size();
        new ArrayList<>(sims.keySet()).forEach(this::stop);
        return n;
    }

    public List<simulation> getAll() {
        return sims.values().stream().sorted(Comparator.comparing(s -> s.simId)).toList();
    }

    public void onModelResult(bioreactorModelResult r) {
        simulation s = sims.get(r.getReactorId());
        if (s != null  && !s.paused) s.latestModelResult = r;
    }

    private void update(simulation sim) {
        if (sim.paused) return;

        simulationStep step = sim.currentStep();

        producer.send(sim.simId,
                bioreactorState.builder()
                        .reactorId(sim.simId)
                        .ph(step.getPh())
                        .temperature(step.getTemperature())
                        .population(0)
                        .hours(sim.realHoursPerUpdate)
                        .build());

        long tick = sim.updateCount.incrementAndGet();
        if (tick >= sim.totalUpdates) {
            sim.state = "COMPLETED";
            if (sim.job != null) sim.job.cancel(false);
            scheduler.schedule(() -> sims.remove(sim.simId), 5, TimeUnit.SECONDS);
        }
    }

    private void cancel(String id) {
        simulation s = sims.remove(id);
        if (s != null && s.job!=null) s.job.cancel(false);
    }
}
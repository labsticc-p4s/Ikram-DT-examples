package com.digitaltwin.twin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

public class Simulation {

    //simulation characteristics
    public final String simId;
    public final String description;

    //for the steps
    public final List<SimulationStep> steps;

    // time model
    public final int simDurationSeconds;
    public final double totalRealDurationHours; //each call of the update in screen how many real minutes it takes
    public final double realMinutesPerUpdate;
    public final long totalUpdates;
    public final long   stepIntervalMs;
    public final long [] updatesPerStep; //how many call of the update function are by each step or scenario
    public final AtomicLong updateCount         = new AtomicLong(0);



    //runtime characteristics
    public volatile String  state = "RUNNING";
    public volatile ModelResult latestModelResult;
    public double temp;
    public double usageMinutes= 0;
    public long startTime= System.currentTimeMillis();
    public long pausedAt= 0;
    public volatile boolean paused = false;
    @JsonIgnore
    public ScheduledFuture<?> job;


    public Simulation(String simId, String description, long stepIntervalMs,
                      int simDurationSeconds, List<SimulationStep> steps){
        this.simId              = simId;
        this.description        = description;
        this.stepIntervalMs     = stepIntervalMs;
        this.simDurationSeconds = simDurationSeconds;
        this.steps = steps;
        this.totalRealDurationHours = steps.stream().mapToDouble(s -> s.realDurationHours).sum();

        double realMinutes = totalRealDurationHours  * 60.0;
        double totalUpdateCall = (simDurationSeconds * 1000.0) / stepIntervalMs;

        this.totalUpdates = Math.round(totalUpdateCall);
        this.realMinutesPerUpdate = realMinutes / totalUpdates;

        this.updatesPerStep = new long[steps.size()];
        long called = 0;
        for (int i = 0; i < steps.size() - 1; i++){
            double fraction = steps.get(i).realDurationHours / totalRealDurationHours;
            updatesPerStep[i] = Math.round(fraction * totalUpdates);
            called += updatesPerStep[i];
        }
        updatesPerStep[steps.size() - 1] = totalUpdates - called;

        this.temp = steps.isEmpty() ? 22.0 : steps.get(0).roomTemp;

    }

    public SimulationStep currentStep() {
        long tick = updateCount.get();
        long boundary = 0;
        for (int i = 0; i < steps.size(); i++) {
            boundary += updatesPerStep[i];
            if (tick < boundary) return steps.get(i);
        }
        return steps.get(steps.size() - 1);
    }

    @JsonProperty("progressPercent")
    public double getProgressPercent() {
        return Math.round(Math.min(100.0, updateCount.get() * 100.0 / ( totalUpdates)) * 10) / 10.0;
    }

    @JsonProperty("realHoursElapsed")
    public double getRealHoursElapsed() {

        return Math.round((usageMinutes / 60.0) * 10.0) / 10.0;
    }
}

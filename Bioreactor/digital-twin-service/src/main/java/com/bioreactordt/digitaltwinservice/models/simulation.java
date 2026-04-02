package com.bioreactordt.digitaltwinservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

public class simulation {

    public final String simId;
    public final String description;
    public final List<simulationStep> steps;

    //time model
    public final int    simDurationSeconds;
    public final double totalRealDurationHours;
    public final double realHoursPerUpdate;
    public final long   totalUpdates;
    public final long   stepIntervalMs;
    public final long[] updatesPerStep;
    public final AtomicLong updateCount = new AtomicLong(0);

    // Runtime
    public volatile String  state = "RUNNING";
    public volatile bioreactorModelResult latestModelResult;
    public long   startTime = System.currentTimeMillis();
    public long   pausedAt  = 0;
    public boolean paused   = false;
    @JsonIgnore
    public ScheduledFuture<?> job;


    public simulation(String simId, String description, long stepIntervalMs,
                      int simDurationSeconds, List<simulationStep> steps) {
        this.simId              = simId;
        this.description        = description;
        this.stepIntervalMs     = stepIntervalMs;
        this.simDurationSeconds = simDurationSeconds;
        this.steps              = steps;

        this.totalRealDurationHours = steps.stream()
                .mapToDouble(simulationStep::getRealDurationHours).sum();

        double totalUpdatesD = (simDurationSeconds * 1000.0) / stepIntervalMs;
        this.totalUpdates        = Math.round(totalUpdatesD);
        this.realHoursPerUpdate  = totalRealDurationHours / totalUpdates;

        this.updatesPerStep = new long[steps.size()];
        long assigned = 0;
        for (int i = 0; i < steps.size() - 1; i++) {
            double fraction = steps.get(i).getRealDurationHours() / totalRealDurationHours;
            updatesPerStep[i] = Math.round(fraction * totalUpdates);
            assigned += updatesPerStep[i];
        }
        updatesPerStep[steps.size() - 1] = totalUpdates - assigned;
    }

    public simulationStep currentStep() {
        long tick = updateCount.get(), boundary = 0;
        for (int i = 0; i < steps.size(); i++) {
            boundary += updatesPerStep[i];
            if (tick < boundary) return steps.get(i);
        }
        return steps.get(steps.size() - 1);
    }

    @JsonProperty("progressPercent")
    public double getProgressPercent() {
        return Math.round(Math.min(100.0, updateCount.get() * 100.0 / totalUpdates) * 10) / 10.0;
    }

    @JsonProperty("realHoursElapsed")
    public double getRealHoursElapsed() {
        return Math.round(updateCount.get() * realHoursPerUpdate * 10.0) / 10.0;
    }
}

package com.bioreactordt.digitaltwinservice.services;

import com.bioreactordt.digitaltwinservice.kafka.SimulationStateProducer;

import com.bioreactordt.digitaltwinservice.models.Simulation;
import com.bioreactordt.digitaltwinservice.models.SimulationStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final SimulationStateProducer producer;

    private final ExecutorService executor = Executors.newCachedThreadPool();


    public String startSimulation(Simulation sim) {
        String experimentId = producer.sendExperimentCreated(sim,
                expId -> executor.submit(() -> runSimulation(sim, expId))
        );
        return experimentId;

    }


    private void runSimulation(Simulation sim, String  experimentId) {

        double actualTotalRealMin = sim.getSteps().stream()
                .mapToDouble(SimulationStep::getRealDurationMin)
                .sum();

        double timeScale = (double) sim.getTotalScreenMin() / actualTotalRealMin;

        for (int i = 0; i < sim.getSteps().size(); i++) {
            SimulationStep step = sim.getSteps().get(i);
            long stepScreenMs = (long) (step.getRealDurationMin() * timeScale * 60_000);
            long tickDelayMs  = stepScreenMs / Math.max(sim.getTicksPerStep(), 1);

            log.info("Step {}/{}: pH={} temp={} | {} ticks every {}ms",
                    i + 1, sim.getSteps().size(), step.getPh(), step.getTemperature(),
                    sim.getTicksPerStep(), tickDelayMs);

            for (int tick = 0; tick < sim.getTicksPerStep(); tick++) {
                producer.sendBioreactorState(experimentId, sim.getReactorId(), step.getPh(), step.getTemperature());
                sleep(tickDelayMs);
            }
        }


        log.info("Simulation complete for reactor {}", sim.getReactorId());
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }


}

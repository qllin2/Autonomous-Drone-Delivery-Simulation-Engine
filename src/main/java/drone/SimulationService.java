package drone;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class SimulationService {

    public enum Status {
        IDLE, RUNNING, FINISHED
    }

    @Autowired
    private MeterRegistry meterRegistry;

    private final AtomicReference<Status> status
            = new AtomicReference<>(Status.IDLE);
    private volatile SimulationResult latestResult = null;

    @Async
    public CompletableFuture<Void> startSimulation(String scenario) {
        status.set(Status.RUNNING);
        Properties props = PropertiesLoader.load("properties/" + scenario + ".properties");
        Simulation sim = new Simulation(props, true);
        sim.run();
        latestResult = new SimulationResult(
                sim.deliveredCount,
                sim.deliveredTotalTime,
                sim.time
        );
        meterRegistry.counter("simulation.parcels.delivered").increment(sim.deliveredCount);
        Gauge.builder("simulation.last.ticks", sim, s -> s.time).description("total ticks of last simulation run")
                .register(meterRegistry);
        status.set(Status.FINISHED);
        return CompletableFuture.completedFuture(null);
    }

    public Status getStatus() {
        return status.get();
    }

    public SimulationResult getLatestResult() {
        return latestResult;
    }
}

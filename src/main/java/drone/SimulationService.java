package drone;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SimulationService {

    public enum Status {
        IDLE, RUNNING, FINISHED
    }

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

package drone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private SimulationInsightService insightService;

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestParam(defaultValue = "test") String scenario) {
        if (simulationService.getStatus() == SimulationService.Status.RUNNING) {
            return ResponseEntity.badRequest().body("Simulation already running");
        }
        simulationService.startSimulation(scenario);
        return ResponseEntity.accepted().body("Simulation started: " + scenario);
    }

    @GetMapping("/status")
    public ResponseEntity<SimulationService.Status> status() {
        return ResponseEntity.ok(simulationService.getStatus());
    }

    @GetMapping("/result")
    public ResponseEntity<SimulationResult> result() {
        SimulationResult result = simulationService.getLatestResult();
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    // Calls OpenAI synchronously — may take 1-3 seconds. Run a simulation first.
    @GetMapping("/insights")
    public ResponseEntity<?> insights() {
        SimulationResult result = simulationService.getLatestResult();
        // 400 instead of 204 here because the caller made a logical mistake:
        // asking for insights without running a simulation is a bad request, not "no content".
        if (result == null) {
            return ResponseEntity.badRequest().body("No simulation result available. Run a simulation first.");
        }
        String insights = insightService.generateInsights(result);
        return ResponseEntity.ok(Map.of(
                "scenario", result.getScenario(),
                "metrics", result,
                "aiInsights", insights
        ));
    }
}

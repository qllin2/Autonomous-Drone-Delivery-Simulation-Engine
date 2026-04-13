package drone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

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
}

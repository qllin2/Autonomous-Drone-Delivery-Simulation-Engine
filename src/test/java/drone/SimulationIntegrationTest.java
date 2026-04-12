package drone;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Simulation integration test - log baseline comparison")
class SimulationIntegrationTest {

    @Test
    @DisplayName("default scenario output should match baseline log (determinism check)")
    void defaultScenarioMatchesBaseline() throws Exception {
        Properties props = PropertiesLoader.load("properties/test.properties");
        props.setProperty("logfile", "build/test-output.txt");
        props.setProperty("timeout", "0");
        new Simulation(props, true).run();

        String actual = Files.readString(Path.of("build/test-output.txt")).trim();
        String expected = Files.readString(
                Path.of("src/test/java/defaults_logfile.txt")).trim();
        assertEquals(expected, actual);
    }
}

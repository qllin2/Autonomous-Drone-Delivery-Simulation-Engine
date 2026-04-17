package drone;

public interface TelemetryPipeline {

    void publish(String event);

    void close();
}

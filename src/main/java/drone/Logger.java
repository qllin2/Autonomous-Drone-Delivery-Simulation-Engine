package drone;

class Logger implements Location.Observer {

    private final TelemetryPipeline pipeline; // Telemetry pipeline instance
    private final Simulation simulation; // Simulation instance

    Logger(TelemetryPipeline pipeline, Simulation simulation) {
        this.simulation = simulation;
        this.pipeline = pipeline;
        Location.addObserver(this);
    }

    @Override
    public void notifyEvent(Location.Id id, String s, Location.DroneEvent e) {
        pipeline.publish(String.format("%5d: %s %s %s%n", simulation.now(), id, s, e));
    }

    public void logEvent(String format, Object... args) {
        pipeline.publish(String.format(format, args));
    }

    public void close() {
        pipeline.close();
    }
}

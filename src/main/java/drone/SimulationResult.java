package drone;

public class SimulationResult {

    private final String scenario;
    private final int deliveredCount;
    private final int totalParcels;
    private final int totalDeliveryTime;
    private final int totalTicks;
    private final int droneCount;
    private final int maxDeliveryTime;
    private final int minDeliveryTime;

    public SimulationResult(String scenario, int deliveredCount, int totalParcels, int totalDeliveryTime,
            int totalTicks, int droneCount, int maxDeliveryTime, int minDeliveryTime) {
        this.scenario = scenario;
        this.deliveredCount = deliveredCount;
        this.totalParcels = totalParcels;
        this.totalDeliveryTime = totalDeliveryTime;
        this.totalTicks = totalTicks;
        this.droneCount = droneCount;
        this.maxDeliveryTime = maxDeliveryTime;
        this.minDeliveryTime = minDeliveryTime;
    }

    public String getScenario() {
        return scenario;
    }

    public int getTotalParcels() {
        return totalParcels;
    }

    public int getDeliveredCount() {
        return deliveredCount;
    }

    public double getAverageDeliveryTime() {
        return deliveredCount == 0 ? 0.0 : (double) totalDeliveryTime / deliveredCount;
    }

    public double getDeliverySuccessRate() {
        return deliveredCount == 0 ? 0.0 : (double) deliveredCount / totalParcels * 100;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public int getDroneCount() {
        return droneCount;
    }

    public int getMaxDeliveryTime() {
        return maxDeliveryTime;
    }

    public int getMinDeliveryTime() {
        return minDeliveryTime;
    }
}

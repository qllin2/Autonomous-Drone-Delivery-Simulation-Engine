package drone;

public class SimulationResult {

    private final int deliveredCount;
    private final int totalDeliveryTime;
    private final int totalTicks;

    public SimulationResult(int deliveredCount, int totalDeliveryTime,
            int totalTicks) {
        this.deliveredCount = deliveredCount;
        this.totalDeliveryTime = totalDeliveryTime;
        this.totalTicks = totalTicks;
    }

    public int getDeliveredCount() {
        return deliveredCount;
    }

    public double getAverageDeliveryTime() {
        return deliveredCount == 0 ? 0.0 : (double) totalDeliveryTime / deliveredCount;
    }

    public int getTotalTicks() {
        return totalTicks;
    }
}

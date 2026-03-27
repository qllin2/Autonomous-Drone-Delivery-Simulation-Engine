package drone;

class DefaultDroneComparator implements DroneComparator {
    private static final int AGING_WEIGHT = 3;
    private static final int FRAGILE_BASE = 24;
    private static final int HEAVY_BASE = 8;
    private static final int STARVATION_THRESHOLD_TICKS = 12;

    @Override
    public int compare(Drone d1, Drone d2) {
        int nowTick = Simulation.now();
        int wait1 = d1.getAccessWaitTicks(nowTick);
        int wait2 = d2.getAccessWaitTicks(nowTick);

        // Hard anti-starvation rule: requests waiting too long are always promoted.
        boolean promoted1 = wait1 >= STARVATION_THRESHOLD_TICKS;
        boolean promoted2 = wait2 >= STARVATION_THRESHOLD_TICKS;
        if (promoted1 != promoted2) {
            return promoted1 ? -1 : 1;
        }
        // If both are promoted, older waiter goes first.
        if (promoted1) {
            if (wait1 != wait2) {
                return Integer.compare(wait2, wait1);
            }
        }

        int score1 = priorityScore(d1, nowTick);
        int score2 = priorityScore(d2, nowTick);
        if (score1 != score2) {
            return Integer.compare(score2, score1);
        }

        // Tie-breaker: drones that are south of the other first.
        Location loc1 = d1.getLocation();
        Location loc2 = d2.getLocation();
        if (loc1 != null && loc2 != null) {
            if (loc1.id instanceof Suburb.AvenueId && !(loc2.id instanceof Suburb.AvenueId)) {
                return -1;
            }
            if (!(loc1.id instanceof Suburb.AvenueId) && loc2.id instanceof Suburb.AvenueId) {
                return 1;
            }
            if (loc1.id instanceof Suburb.AvenueId && loc2.id instanceof Suburb.AvenueId) {
                Suburb.AvenueId ave1 = (Suburb.AvenueId) loc1.id;
                Suburb.AvenueId ave2 = (Suburb.AvenueId) loc2.id;
                return Integer.compare(ave1.numSouth, ave2.numSouth);
            }
        }

        // Keep stable-sort FIFO order for remaining ties.
        return 0;
    }

    private int priorityScore(Drone drone, int nowTick) {
        int base = 0;
        if (drone.wasCarryingFragile()) {
            base += FRAGILE_BASE;
        } else if (drone.wasCarryingHeavy()) {
            base += HEAVY_BASE;
        }
        int ageBoost = drone.getAccessWaitTicks(nowTick) * AGING_WEIGHT;
        return base + ageBoost;
    }
}

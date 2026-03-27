package drone;

interface MovementStrategy {

    // returns true if the drone should move in the current tick
    public boolean shouldMove(Drone drone);
}

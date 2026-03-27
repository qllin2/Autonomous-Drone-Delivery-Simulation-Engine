package drone;

class NormalMovementStrategy implements MovementStrategy {

    @Override
    public boolean shouldMove(Drone drone) {
        return true;
    }
}

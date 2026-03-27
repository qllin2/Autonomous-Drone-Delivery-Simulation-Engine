package drone;

class FragileMovementStrategy implements MovementStrategy {

    private boolean shouldMoveInCurrentTick = true;

    @Override
    public boolean shouldMove(Drone drone) {
        // half speed move
        shouldMoveInCurrentTick = !shouldMoveInCurrentTick;
        return shouldMoveInCurrentTick;
    }
}

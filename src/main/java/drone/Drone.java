package drone;

class Drone implements Tickable {

    static int count = 1;
    final String id;
    final DispatchCentre dispatchCentre;
    final Suburb suburb;
    Location location;
    Parcel parcel = null;

    enum State {
        WaitingForDispatch, TransitToSuburb, TransitToDelivery, Delivering, TransitToExit, TransitToCentre, Recharge
    }
    State state = State.WaitingForDispatch;
    int transDuration; // Elapsed time before next transition occurs
    private boolean wasCarryingFragile = false; // true if this drone was carrying a fragile parcel in this trip
    private boolean wasCarryingHeavy = false; // true if this drone was carrying a heavy parcel in this trip
    private boolean hasRequestedAccess = false;
    private int accessWaitStartTick = -1;
    private MovementStrategy movementStrategy;

    Drone(DispatchCentre dispatchCentre, Suburb suburb) {
        this.dispatchCentre = dispatchCentre;
        this.suburb = suburb;
        this.id = "D" + count++;
        location = null;
        Simulation.register(this);
    }

    public void tick() {
        Location nextLocation;
        switch (state) {
            case WaitingForDispatch:
                dispatchCentre.requestDispatch(this);
                break;
            case TransitToSuburb:
                if (transDuration > 0) {
                    // handle the half speed move
                    if (movementStrategy.shouldMove(this)) {
                        transDuration--;
                    }
                } else {
                    dispatchCentre.requestAccess(this, suburb.getEntry());
                }
                break;
            case TransitToDelivery:
                // handle the half speed move
                if (!movementStrategy.shouldMove(this)) {
                    break; // skip the move
                }

                // Counterclockwise: south to parcel street then east to parcel house
                nextLocation = location.getRoad(Suburb.Direction.EAST);
                // East - looking for delivery location - must be a street
                if (nextLocation == null || !((Suburb.StreetId) nextLocation.id).sameStreet(parcel.myStreet())) {
                    // not currently next to parcel address street
                    nextLocation = location.getRoad(Suburb.Direction.SOUTH);
                }
                assert nextLocation != null :
                        "Reached " + location.id + " without finding street:" + parcel.myStreet();
                dispatchCentre.requestAccess(this, nextLocation);
                break;
            case Delivering:
                // start delivery fragile parcel move
                if (parcel != null && !movementStrategy.shouldMove(this)) {
                    break; // skip the move
                }

                // normal delivery move
                if (parcel == null) {
                    location.endDelivery();
                    state = State.TransitToExit;
                } else {
                    location.startDelivery();
                    Simulation.deliver(parcel);
                    parcel = null;
                    movementStrategy = new NormalMovementStrategy();
                }
                break;
            case TransitToExit:
                // Counterclockwise: east to Back Ave then north to exit
                nextLocation = location.getRoad(Suburb.Direction.EAST);
                if (nextLocation == null) {
                    nextLocation = location.getRoad(Suburb.Direction.NORTH);
                }
                assert nextLocation != null : "Can't go east or north from " + location.id;
                dispatchCentre.requestAccess(this, nextLocation);
                break;
            case TransitToCentre:
                if (location != null) {
                    location.departDrone();
                }
                if (transDuration > 0) {
                    transDuration--;
                } else {
                    state = State.Recharge;
                }
                break;
            case Recharge:
                state = State.WaitingForDispatch;
                wasCarryingFragile = false;
                wasCarryingHeavy = false;
                break;
        }
    }

    void dispatch(Parcel parcel) {
        assert state == State.WaitingForDispatch : id + " dispatched when not waiting for dispatch";
        this.parcel = parcel;
        transDuration = dispatchCentre.timeToSuburb;
        state = State.TransitToSuburb;

        // check if the parcel is fragile
        if (parcel.isFragile()) {
            this.movementStrategy = new FragileMovementStrategy();
            wasCarryingFragile = true;
        } else {
            this.movementStrategy = new NormalMovementStrategy();
        }

        // check if the parcel is heavy
        wasCarryingHeavy = dispatchCentre.isHeavy(parcel);
    }

    void grantAccess(Location location) {
        switch (state) {
            case TransitToSuburb:
                if (this.location != null) {
                    this.location.departDrone();
                }
                location.arriveDrone(this);
                state = State.TransitToDelivery;
                break;
            case TransitToDelivery:
                if (this.location != null) {
                    this.location.departDrone();
                }
                location.arriveDrone(this);
                if (location.id.deliveryAddress(parcel.myStreet(), parcel.myHouse())) {
                    state = State.Delivering;
                }
                break;
            case TransitToExit:
                if (this.location != null) {
                    this.location.departDrone();
                }
                location.arriveDrone(this);
                if (location == suburb.getExit()) {
                    state = State.TransitToCentre;
                    transDuration = dispatchCentre.timeToSuburb;
                }
                break;
            default:
                assert false : id + " access granted to " + location.id + " in non-requesting state " + state;
        }
    }

    public String toString() {
        return id;
    }

    void setLocation(Location location) {
        this.location = location;
    }

    Location getLocation() {
        return location;
    }

    public void add(Parcel item) {
        parcel = item;
    }

    public boolean wasCarryingFragile() {
        return wasCarryingFragile;
    }

    public boolean wasCarryingHeavy() {
        return wasCarryingHeavy;
    }

    public boolean hasRequestedAccess() {
        return hasRequestedAccess;
    }

    public void setHasRequestedAccess(boolean hasRequestedAccess) {
        this.hasRequestedAccess = hasRequestedAccess;
    }

    public void markAccessWaitStart(int tick) {
        if (accessWaitStartTick < 0) {
            accessWaitStartTick = tick;
        }
    }

    public void clearAccessWait() {
        accessWaitStartTick = -1;
    }

    public int getAccessWaitTicks(int nowTick) {
        if (accessWaitStartTick < 0) {
            return 0;
        }
        return Math.max(0, nowTick - accessWaitStartTick);
    }

}

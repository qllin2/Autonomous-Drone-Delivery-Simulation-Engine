package drone;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

class DispatchCentre implements Tickable {

    final int numdrones;
    public final int timeToSuburb;
    public final int weightThreshold;
    final Queue<Parcel> waitingForDelivery;
    final Set<Drone> drones;
    private final DroneComparator droneComparator;
    private final LocationAccessManager accessManager;
    private final Simulation simulation;

    public boolean someItems() {
        return !waitingForDelivery.isEmpty();
    }

    DispatchCentre(Suburb suburb, int timeToSuburb, int numdrones, int weightThreshold, Simulation simulation) {
        this.timeToSuburb = timeToSuburb; // Distance away suburb is from dispatch centre
        this.weightThreshold = weightThreshold;
        this.numdrones = numdrones;
        this.simulation = simulation;
        this.droneComparator = new DefaultDroneComparator(simulation);
        this.accessManager = new LocationAccessManager(droneComparator, simulation);

        waitingForDelivery = new LinkedList<>();
        drones = new HashSet<>();
        for (int i = 0; i < numdrones; i++) {
            drones.add(new Drone(this, suburb, simulation));
        }
        simulation.register(this);

        accessManager.initializeRequestMap(suburb);
    }

    void arrive(List<Parcel> parcels) {
        for (Parcel parcel : parcels) {
            waitingForDelivery.add(parcel);
            String s = "Arrived: " + parcel;
            System.out.println(s);
            simulation.logger.logEvent("%5d: %s\n", simulation.now(), s);
        }
    }

    boolean isHeavy(Parcel parcel) {
        return parcel.myWeight() > weightThreshold;
    }

    public void requestDispatch(Drone drone) {
        if (!waitingForDelivery.isEmpty()) {
            drone.dispatch(waitingForDelivery.remove());
            drones.remove(drone);
        } else {
            drones.add(drone);  // Track waiting drones
        }
    }

    public boolean allDronesBack() {
        return drones.size() == numdrones;
    }

    //  Cache the access request for processing at the end of the time step
    void requestAccess(Drone drone, Location location) {
        accessManager.requestAccess(drone, location);
    }

    public void tick() {
        // Ticked after all drones
        accessManager.processAccessRequests();
    }

}

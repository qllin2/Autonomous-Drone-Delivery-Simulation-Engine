package drone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class LocationAccessManager {

    private final Map<Location, List<Drone>> requests = new LinkedHashMap<>();
    private final DroneComparator droneComparator;

    public LocationAccessManager(DroneComparator droneComparator) {
        this.droneComparator = droneComparator;
    }

    public void initializeRequestMap(Suburb suburb) {
        // add "backwards path" locations to request map
        for (Location l = suburb.backAvenue; l != null; l = l.getRoad(Suburb.Direction.SOUTH)) {
            requests.put(l, new ArrayList<>());
        }
        for (int i = 0; i < suburb.NUMSTREETS; i++) {
            Location e = suburb.streets[i];
            for (int j = 0; j < suburb.NUMHOUSES; j++) {
                e = e.getRoad(Suburb.Direction.EAST);
            }
            for (int j = 0; j < suburb.NUMHOUSES; j++) {
                e = e.getRoad(Suburb.Direction.WEST);
                requests.put(e, new ArrayList<>());
            }
        }
        Location e = suburb.outAvenue;
        while (e.getRoad(Suburb.Direction.SOUTH) != null) {
            e = e.getRoad(Suburb.Direction.SOUTH);
        }
        for (Location l = e; l != null; l = l.getRoad(Suburb.Direction.NORTH)) {
            requests.put(l, new ArrayList<>());
        }
    }

    public void requestAccess(Drone drone, Location location) {
        if (!drone.hasRequestedAccess()) {
            drone.setHasRequestedAccess(true);
            drone.markAccessWaitStart(Simulation.now());
            requests.get(location).add(drone);
        }
    }

    public void processAccessRequests() {
        // process requests in clockwise order (reverse travel order)
        for (Location location : requests.keySet()) {
            List<Drone> waitingDrones = requests.get(location);
            if (location.drone == null && !waitingDrones.isEmpty()) {
                waitingDrones.sort(droneComparator);
                Drone drone = waitingDrones.remove(0);
                drone.grantAccess(location);
                drone.setHasRequestedAccess(false);
                drone.clearAccessWait();
            }
        }
    }
}

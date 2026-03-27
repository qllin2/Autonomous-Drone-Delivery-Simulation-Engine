package drone;

import java.util.Comparator;

interface DroneComparator extends Comparator<Drone> {

    @Override
    public int compare(Drone d1, Drone d2);
}

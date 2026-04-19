package drone;

// Check that maxweight (of parcel) is less than or equal to the maxcapacity of drone.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class Simulation {

    Logger logger;

    final List<Tickable> entities = new ArrayList<>();

    void register(Tickable entity) {
        entities.add(entity);
    }
    final Map<Integer, List<Parcel>> waitingToArrive = new HashMap<>();
    int time = 0;
    final int endArrival;
    final DispatchCentre dispatchCentre;
    int timeout;

    int deliveredCount = 0;
    int deliveredTotalTime = 0;
    int totalParcels = 0;
    int droneCount = 0;
    int maxDeliveryTime = Integer.MIN_VALUE;
    int minDeliveryTime = Integer.MAX_VALUE;

    public void deliver(Parcel parcel) {
        String s = "Delivered: " + parcel;
        System.out.println(s);
        logger.logEvent("%5d: %s\n", now(), s);;
        deliveredCount++;
        int deliveryTime = now() - parcel.myArrival();
        deliveredTotalTime += deliveryTime;
        if (deliveryTime > maxDeliveryTime) {
            maxDeliveryTime = deliveryTime;
        }
        if (deliveryTime < minDeliveryTime) {
            minDeliveryTime = deliveryTime;
        }
    }

    void addToArrivals(int arrivalTime, Parcel item) {
        // System.out.println(item.toString());
        if (waitingToArrive.containsKey(arrivalTime)) {
            waitingToArrive.get(arrivalTime).add(item);
        } else {
            LinkedList<Parcel> items = new LinkedList<>();
            items.add(item);
            waitingToArrive.put(arrivalTime, items);
        }
    }

    Simulation(Properties properties, boolean headless, TelemetryPipeline externalPipeline) {
        int seed = Integer.parseInt(properties.getProperty("seed", "30006"));
        this.endArrival = Integer.parseInt(properties.getProperty("parcel.endarrival", "5"));
        int numParcels = Integer.parseInt(properties.getProperty("parcel.parcels", "4"));
        int maxWeight = Integer.parseInt(properties.getProperty("parcel.maxweight", "15"));
        int weightThreshold = Integer.parseInt(properties.getProperty("parcel.weightthreshold", "12"));
        int fragile1in = Integer.parseInt(properties.getProperty("parcel.fragile1in", "1000"));
        int timeToSuburb = Integer.parseInt(properties.getProperty("parcel.timetosuburb", "4"));
        int numStreets = Integer.parseInt(properties.getProperty("suburb.streets", "2"));
        int numHouses = Integer.parseInt(properties.getProperty("suburb.housesperstreet", "3"));
        int numdrones = Integer.parseInt(properties.getProperty("drone.number", "1"));
        timeout = Integer.parseInt(properties.getProperty("timeout", "600"));
        String logfile = properties.getProperty("logfile", "logfile.txt");
        this.droneCount = numdrones;
        this.totalParcels = numParcels;

        // Falls back to in-memory pipeline when no Kafka profile is active,
        // so tests and standalone runs work without a live Kafka broker.
        TelemetryPipeline pipeline = (externalPipeline != null)
                ? externalPipeline
                : new InMemoryTelemetryPipeline(logfile);
        logger = new Logger(pipeline, this); // Simulation instance is passed to Logger
        logger.logEvent("%5d: Simulation parameters - seed=%d\n", now(), seed);
        Random random = new Random(seed);

        Suburb suburb = new Suburb(numStreets, numHouses);
        if (!headless) {
            new SuburbView(numStreets, numHouses); // Registers itself as a Location observer
        }

        dispatchCentre = new DispatchCentre(suburb, timeToSuburb, numdrones, weightThreshold, this);
        for (int i = 0; i < numParcels; i++) { // Generate parcels
            int arrivalTime = random.nextInt(endArrival) + 1;
            int street = random.nextInt(suburb.NUMSTREETS) + 1;
            int house = random.nextInt(suburb.NUMHOUSES) + 1;
            int weight = random.nextInt(maxWeight) + 1;
            boolean fragile = random.nextInt(fragile1in) == 0;
            addToArrivals(arrivalTime, new Parcel(street, house, arrivalTime, weight, fragile));
        }
    }

    public int now() {
        return time;
    }

    void step() {
        // External events
        if (waitingToArrive.containsKey(time)) {
            dispatchCentre.arrive(waitingToArrive.get(time));
        }
        // Internal events
        for (Tickable entity : entities) {
            entity.tick();
        }
    }

    void run() {
        while (time++ <= endArrival || dispatchCentre.someItems() || !dispatchCentre.allDronesBack()) {
            step();
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                // System.out.printf("Sleep interrupted!\n");
            }
        }
        logger.logEvent("%5d Finished: Items delivered = %d; Average time for delivery = %.2f%n",
                now(), deliveredCount, (float) deliveredTotalTime / deliveredCount);
        logger.close();
    }

}

package drone;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class Logger implements Location.Observer {

    private static final String POISON_PILL = "__LOGGER_STOP__";
    private final PrintWriter printWriter;
    private final BlockingQueue<String> queue;
    private final Thread writerThread;
    private final Simulation simulation; // Simulation instance

    Logger(String filename, Simulation simulation) {
        this.simulation = simulation;
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        printWriter = new PrintWriter(fileWriter);
        queue = new ArrayBlockingQueue<>(8192);
        writerThread = new Thread(this::drainQueue, "telemetry-writer");
        writerThread.setDaemon(true);
        Location.addObserver(this);
    }

    public void start() {
        writerThread.start();
    }

    private void drainQueue() {
        try {
            while (true) {
                String line = queue.take();
                if (POISON_PILL.equals(line)) {
                    break;
                }
                printWriter.print(line);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            printWriter.flush();
            printWriter.close();
        }
    }

    private void enqueue(String entry) {
        try {
            if (!queue.offer(entry, 100, TimeUnit.MILLISECONDS)) {
                // Avoid blocking simulation on I/O overload.
                queue.poll();
                queue.offer(entry);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void notifyEvent(Location.Id id, String s, Location.DroneEvent e) {
        enqueue(String.format("%5d: %s %s %s%n", simulation.now(), id, s, e));
    }

    public void logEvent(String format, Object... args) {
        enqueue(String.format(format, args));
    }

    public void close() {
        enqueue(POISON_PILL);
        try {
            writerThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

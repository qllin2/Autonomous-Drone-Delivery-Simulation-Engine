package drone;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class InMemoryTelemetryPipeline implements TelemetryPipeline {

    private static final String POISON_PILL = "__LOGGER_STOP__";
    private final PrintWriter printWriter;
    private final BlockingQueue<String> queue;
    private final Thread writerThread;

    InMemoryTelemetryPipeline(String filename) {
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
        writerThread.start();
    }

    @Override
    public void publish(String event) {
        try {
            if (!queue.offer(event, 100, TimeUnit.MILLISECONDS)) {
                // Avoid blocking simulation on I/O overload.
                queue.poll();
                queue.offer(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        publish(POISON_PILL);
        try {
            writerThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
}

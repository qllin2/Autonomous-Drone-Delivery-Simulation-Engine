package drone;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class TelemetryConsumer {

    @KafkaListener(topics = "drone-telemetry-events", groupId = "drone-logger")
    public void consume(ConsumerRecord<String, String> record) {
        System.out.printf("[TELEMETRY] offset=%d  %s%n", record.offset(), record.value());
    }
}

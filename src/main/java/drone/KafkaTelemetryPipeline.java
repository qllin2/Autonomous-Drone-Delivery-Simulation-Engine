package drone;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaTelemetryPipeline implements TelemetryPipeline {

    private static final String TOPIC = "drone-telemetry-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    KafkaTelemetryPipeline(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String event) {
        kafkaTemplate.send(TOPIC, event);
    }

    @Override
    public void close() {
        kafkaTemplate.flush();
    }
}

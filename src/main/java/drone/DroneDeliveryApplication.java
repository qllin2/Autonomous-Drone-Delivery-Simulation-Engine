package drone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DroneDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneDeliveryApplication.class, args);
    }
}

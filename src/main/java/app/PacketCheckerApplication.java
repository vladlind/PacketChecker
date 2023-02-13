package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PacketCheckerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PacketCheckerApplication.class, args);
    }
}

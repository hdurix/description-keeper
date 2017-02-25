package fr.hippo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AutoConfigurationPackage
public class DescriptionKeeperApplication {

    public static void main(String[] args) {
        SpringApplication.run(DescriptionKeeperApplication.class, args);
    }
}
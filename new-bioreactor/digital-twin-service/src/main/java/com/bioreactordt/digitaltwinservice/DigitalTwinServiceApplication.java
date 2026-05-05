package com.bioreactordt.digitaltwinservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DigitalTwinServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }
}

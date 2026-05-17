package com.digitaltwin.physical;

import com.digitaltwin.physical.model.LanpConfig;
import com.digitaltwin.physical.service.LampConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class PhysicalEntityApplication implements CommandLineRunner {

    private final LampConfigService conf;

    @Value("${lamp.id}")                               private String  lampId;
    @Value("${lamp.host}")                             private String  host;
    @Value("${server.port}")                           private int     port;
    @Value("${lamp.description:}")                     private String  description;
    @Value("${lamp.room}") private String room;
    @Value("${lamp.hasTemperatureSensor:true}")        private boolean hasTemperatureSensor;
    @Value("${lamp.hasRoomTempSensor:true}")           private boolean hasRoomTempSensor;

    @Value("${lamp.maxWatts:60.0}")          private double maxWatts;
    @Value("${lamp.maxLifeMinutes:1500000}") private double maxLifeMinutes;

    public static void main(String[] args) {
        SpringApplication.run(PhysicalEntityApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

    @Override
    public void run(String... args) {
        conf.addConfLamp(LanpConfig.builder()
                .lampId(lampId)
                .host(host)
                .port(port)
                .description(description)
                .room(room)
                .hasTemperatureSensor(hasTemperatureSensor)
                .hasRoomTempSensor(hasRoomTempSensor)
                .maxWatts(maxWatts)
                .maxLifeMinutes(maxLifeMinutes)
                .build());
    }
}
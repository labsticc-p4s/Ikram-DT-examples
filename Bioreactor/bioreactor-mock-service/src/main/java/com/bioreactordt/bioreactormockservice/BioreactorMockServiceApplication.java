package com.bioreactordt.bioreactormockservice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class BioreactorMockServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(BioreactorMockServiceApplication.class, args);
    }


    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

}

package com.bioreactordt.strainservice;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class StrainServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(StrainServiceApplication.class, args);
    }

}

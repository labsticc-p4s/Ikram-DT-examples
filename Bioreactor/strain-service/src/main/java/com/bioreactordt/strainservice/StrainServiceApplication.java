package com.bioreactordt.strainservice;
import lombok.RequiredArgsConstructor;
import com.bioreactordt.strainservice.models.StrainConfig;
import com.bioreactordt.strainservice.services.StrainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class StrainServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(StrainServiceApplication.class, args);
    }

}

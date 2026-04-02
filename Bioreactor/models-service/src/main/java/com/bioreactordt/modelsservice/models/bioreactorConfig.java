package com.bioreactordt.modelsservice.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class bioreactorConfig {

    private String reactorId;

    private double populationInit;

    //caracteristiques de la souche
    private double muFactor; //la vitesse de croissance

    private double minPh;
    private double maxPh;
    private double optPh;

    private double minTemp;
    private double maxTemp;
    private double optTemp;

    private double populationMax;
    private double latency;





}

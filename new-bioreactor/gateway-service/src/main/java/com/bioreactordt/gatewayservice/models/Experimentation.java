package com.bioreactordt.gatewayservice.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experimentation {

    private String reactorId;
    private String condInit;
    private String populationModel;
    private String phModel;
    private String tempModel;


}

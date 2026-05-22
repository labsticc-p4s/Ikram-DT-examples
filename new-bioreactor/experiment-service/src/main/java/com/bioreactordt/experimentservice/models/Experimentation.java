package com.bioreactordt.experimentservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experimentation {

    private String experimentId;
    private String reactorId;
    private String condInit;
    private String populationModel;
    private String phModel;
    private String tempModel;
    private String source;


}


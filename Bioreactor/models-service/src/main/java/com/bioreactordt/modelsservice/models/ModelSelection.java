package com.bioreactordt.modelsservice.models;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelSelection {

    private String reactorId;
    private String phModel;
    private String temperatureModel;
    private String populationModel;
}

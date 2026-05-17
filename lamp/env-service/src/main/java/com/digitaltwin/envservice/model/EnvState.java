package com.digitaltwin.envservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvState {

    private String roomId; //classify the env by room each has a temp
    private double roomTemp = 22.0;
}

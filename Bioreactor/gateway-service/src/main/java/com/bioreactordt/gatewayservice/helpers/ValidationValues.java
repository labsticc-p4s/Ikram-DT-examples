package com.bioreactordt.gatewayservice.helpers;

import com.bioreactordt.gatewayservice.models.EnrichedBioreactorState;
import org.springframework.stereotype.Component;

@Component
public class ValidationValues {
    //delete appres
    public EnrichedBioreactorState validate(EnrichedBioreactorState s){

        if (s.getPh() < 0 || s.getPh() > 14) {
            s.setValid(false);
            s.setInvalidReason("pH is out of range [0,14]: " + s.getPh());
            return s;
        }

        if (s.getTemperature() < -10 || s.getTemperature() > 100) {
            s.setValid(false);
            s.setInvalidReason("Temperature is out of range [-10,100]: " + s.getTemperature());
            return s;
        }

        if (s.getStrainIds() == null || s.getStrainIds().isEmpty()) {
            s.setValid(false);
            s.setInvalidReason("No strains were registered for the bioreactor: " + s.getReactorId());
            return s;
        }
        s.setValid(true);
        return s;
    }
}

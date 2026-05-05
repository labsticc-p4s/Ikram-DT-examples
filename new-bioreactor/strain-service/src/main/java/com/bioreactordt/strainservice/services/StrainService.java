package com.bioreactordt.strainservice.services;

import com.bioreactordt.strainservice.models.InitialStrain;
import com.bioreactordt.strainservice.models.StrainFamily;
import greycat.GreyCat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class StrainService {

    private final String greycatUrl;
    private GreyCat greycat;




    public StrainService(@Value("${greycat.url}") String greycatUrl) {
        this.greycatUrl = greycatUrl;
    }

    private synchronized GreyCat greycat() {
        if (greycat == null) {
            try {
                greycat = new GreyCat(greycatUrl, null, false, false);
                log.info("Connected to GreyCat at {}", greycatUrl);
            } catch (Exception e) {
                log.error("Failed to connect to GreyCat at {}: {}", greycatUrl, e.getMessage());
                throw new RuntimeException("Failed to connect to GreyCat at " + greycatUrl, e);
            }
        }
        return greycat;
    }





    public void saveFamily(StrainFamily f) {
        try{
            greycat().call("project::family_save", f.getStrainId(), f.getName(), f.getMuMax(), f.getLatency(), f.getPhMin(), f.getPhOpt(), f.getPhMax(), f.getTempMin(), f.getTempOpt(), f.getTempMax());
            log.info("Strain Family {} registered", f.getStrainId());

        }catch (Exception e) {
            log.error("Failed to save a family: {}", e.getMessage());
        }
    }


    public void saveInitialStrain(InitialStrain initial) {
        try {
            log.info("Received initial strain: condId={}, familyIds={}", initial.getCondId(), initial.getFamilyIds());

            String familyId = initial.getFamilyIds().get(0);
            log.info("using familyid {}", familyId);

            greycat().call("project::save_initial_strain", initial.getCondId(), initial.getPopulationInit(), initial.getPopulationMax(), familyId);

        } catch (Exception e) {
            log.error("failed to link cond init to strain {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public InitialStrain getInitialStrain(String condId) throws IOException {
        Object raw = greycat().call("project::get_initial_strain", condId);

        String str = raw.toString()
                .replace("core::Array[", "")
                .replace("]", "");

        String[] parts = str.split(",");
       // log.info(parts[0]);
        return new InitialStrain(
                parts[0],
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                List.of(parts[3])
        );
    }





}
































// raw.toString() = "core::Array[COND-001,1000000.0,1.0E10,FAM-001]"

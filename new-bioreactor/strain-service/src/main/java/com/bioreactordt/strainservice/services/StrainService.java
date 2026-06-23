package com.bioreactordt.strainservice.services;

import com.bioreactordt.shared.GreycatClient;
import com.bioreactordt.strainservice.models.InitialStrain;
import com.bioreactordt.strainservice.models.StrainFamily;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StrainService {


    private final GreycatClient greycat;


    public void saveFamily(StrainFamily f) throws Exception {
        greycat.call("project::family_save",
                f.getStrainId(), f.getName(), f.getMuMax(), f.getLatency(),
                f.getPhMin(), f.getPhOpt(), f.getPhMax(),
                f.getTempMin(), f.getTempOpt(), f.getTempMax());
        log.info("family saved: {}", f.getStrainId());
    }




    public void saveInitialStrain(InitialStrain s) throws Exception {
        greycat.call("project::save_initial_strain",
                s.getCondId(), s.getPopulationInit(), s.getPopulationMax(),
                s.getFamilyIds().get(0));
        log.info("initial strain saved: {}", s.getCondId());
    }





    public InitialStrain getInitialStrain(String condId) throws Exception {
        Map<String, Object> r = greycat.callMap("project::get_initial_strain", condId);
        if (r == null) return null;
        String familyName = (String) r.get("familyName");
        return new InitialStrain(condId,
                d(r.get("populationInit")), d(r.get("populationMax")),
                familyName == null ? List.of() : List.of(familyName));
    }


    public List<StrainFamily> getAllFamilies() throws Exception {
        return greycat.callList("project::get_all_families").stream().map(r ->
                StrainFamily.builder()
                        .strainId((String) r.get("strainId"))
                        .name    ((String) r.get("name"))
                        .muMax   (d(r.get("muMax")))    .latency(d(r.get("latency")))
                        .phMin   (d(r.get("phMin")))    .phOpt  (d(r.get("phOpt")))   .phMax  (d(r.get("phMax")))
                        .tempMin (d(r.get("tempMin")))  .tempOpt(d(r.get("tempOpt"))) .tempMax(d(r.get("tempMax")))
                        .build()
        ).toList();
    }

    public List<InitialStrain> getAllInitConds() throws Exception {
        return greycat.callList("project::get_all_init_conds").stream().map(r ->
                new InitialStrain((String) r.get("condId"),
                        d(r.get("populationInit")), d(r.get("populationMax")),
                        List.of((String) r.get("familyId")))
        ).toList();
    }



    private double d(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }



}
































// raw.toString() = "core::Array[COND-001,1000000.0,1.0E10,FAM-001]"

package com.bioreactordt.digitaltwinservice.services;

import com.bioreactordt.digitaltwinservice.kafka.twinStateProducer;
import com.bioreactordt.digitaltwinservice.models.BioreactorModelResult;
import com.bioreactordt.digitaltwinservice.models.BioreactorState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class synchroService {

    private final RestTemplate      restTemplate;
    private final twinStateProducer producer;

    @Getter private BioreactorModelResult modelResult;
    @Getter private volatile boolean      twinned   = false;
    @Getter private volatile String       reactorId = null;
    @Getter private volatile List<String> strainIds = List.of();

    public void twinOrUntwin(boolean enabled, String reactorId, List<String> strainIds) {
        this.twinned   = enabled;
        this.reactorId = enabled ? reactorId : null;
        this.strainIds = enabled ? strainIds : List.of();
        if (!enabled) modelResult = null;
        log.info("Sync {}: reactorId={} strains={}", enabled ? "ENABLED" : "DISABLED", reactorId, strainIds);
    }


    public void forwardWithChosenStrains(BioreactorState incoming) {
        if (!twinned || reactorId == null) return;
        if (!reactorId.equals(incoming.getReactorId())) return;
        producer.send(reactorId,
                BioreactorState.builder()
                        .reactorId(reactorId)
                        .strainIds(strainIds)
                        .ph(incoming.getPh())
                        .temperature(incoming.getTemperature())
                        .hours(incoming.getHours())
                        .source("PHYSICAL")
                        .build());
    }


    public void onModelResult(BioreactorModelResult r) {
        if (!twinned) return;
        if (reactorId != null && !reactorId.equals(r.getReactorId())) return;
        modelResult = r;
    }


}
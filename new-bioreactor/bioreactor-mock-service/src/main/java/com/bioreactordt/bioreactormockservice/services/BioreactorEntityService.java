package com.bioreactordt.bioreactormockservice.services;

import com.bioreactordt.bioreactormockservice.kafka.BioreactorProducer;
import com.bioreactordt.bioreactormockservice.models.BioreactorState;
import com.bioreactordt.bioreactormockservice.replay.RawDataSheet;
import com.bioreactordt.bioreactormockservice.replay.TimeSerie;
import greycat.GreyCat;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class BioreactorEntityService {

    private final String greycatUrl;
    private GreyCat greycat;

    @Value("${reactor.id}")   private String reactorId;

    public BioreactorEntityService(@Value("${greycat.url}") String greycatUrl) {
        this.greycatUrl = greycatUrl;
    }

    private synchronized GreyCat greycat() {
        if (greycat == null) try {
            greycat = new GreyCat(greycatUrl, null, false, false);
            log.info("Connected to GreyCat Successfully at {}", greycatUrl);
        } catch (Exception e) {
            log.error("Failed to connect to GreyCat at {}: {}", greycatUrl, e.getMessage());
            throw new RuntimeException("Failed to connect to GreyCat at " + greycatUrl, e);
        }
        return greycat;
    }


    @PostConstruct
    public void register() {
        try {
            greycat().call("project::bioreactor_save", reactorId);
            log.info("Bioreactor added successfully {}", reactorId);
        } catch (Exception e) {
            log.error("failed to add bioreactor {}", e.getMessage());
            greycat = null;
        }
    }

}
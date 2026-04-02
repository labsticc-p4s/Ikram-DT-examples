package com.bioreactordt.shadowservice.services;

import com.bioreactordt.shadowservice.models.bioreactorModelResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class shadowService {
    private static final int MAX = 10_000;

    private final List<bioreactorModelResult> physicalLogs = Collections.synchronizedList(new ArrayList<>());
    private final List<bioreactorModelResult> simLogs = Collections.synchronizedList(new ArrayList<>());

    private final AtomicLong counter = new AtomicLong(0);

    @Getter
    private volatile boolean twinned    = false;
    @Getter
    private volatile String  reactorId  = null;

    public void setTwinned(boolean enabled, String reactorId) {
        this.twinned   = enabled;
        this.reactorId = enabled ? reactorId : null;
    }


    public void store(bioreactorModelResult r) {
        r.setTupleId(counter.incrementAndGet());
        if ("PHYSICAL".equals(r.getSource())) {
            if (!twinned) return;
            if (reactorId != null && !reactorId.equals(r.getReactorId())) return;
            r.setTupleId(counter.incrementAndGet());
            if (physicalLogs.size() >= MAX) physicalLogs.remove(0);
            physicalLogs.add(r);
        } else {
            r.setTupleId(counter.incrementAndGet());
            if (simLogs.size() >= MAX) simLogs.remove(0);
            simLogs.add(r);
        }
    }

    public List<bioreactorModelResult> getPhysicalLastN(int n) {
        synchronized (physicalLogs) {
            int size = physicalLogs.size();
            return new ArrayList<>(physicalLogs.subList(Math.max(0, size - n), size));
        }
    }

    public List<bioreactorModelResult> getSimLogs() {
        return new ArrayList<>(simLogs);
    }

    public List<bioreactorModelResult> getSimHistoryForReactor(String reactorId) {
        synchronized (simLogs) {
            return simLogs.stream()
                    .filter(r -> reactorId.equals(r.getReactorId()))
                    .toList();
        }
    }

    public void clear() {
        physicalLogs.clear();
        simLogs.clear();
        counter.set(0);
    }

}

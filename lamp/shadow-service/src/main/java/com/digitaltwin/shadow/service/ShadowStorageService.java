package com.digitaltwin.shadow.service;
import com.digitaltwin.shadow.model.ModelResult;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ShadowStorageService {

    private static final int MAX = 10_000;
    //store only when twinned, only the synchro physical logs
    private final List<ModelResult> physicalLogs = Collections.synchronizedList(new ArrayList<>());
    //simulation logs
    private final List<ModelResult> simLogs = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong counter = new AtomicLong(0);
    @Getter
    private volatile boolean twinned = false;
    @Getter
    private volatile String  twinnedLampId = null;   // ← add this

    public void setTwinned(boolean enabled, String lampId) {
        this.twinned = enabled;
        this.twinnedLampId = enabled ? lampId : null;
    }

    public void store(ModelResult r) {
        if ("PHYSICAL".equals(r.getSource())) {
            if (!twinned) return;
            if (twinnedLampId != null && !twinnedLampId.equals(r.getLampId())) return;
            r.setTupleId(counter.incrementAndGet());
            if (physicalLogs.size() >= MAX) physicalLogs.remove(0);
            physicalLogs.add(r);
        } else {
            // SIMULATION and any other source
            r.setTupleId(counter.incrementAndGet());
            if (simLogs.size() >= MAX) simLogs.remove(0);
            simLogs.add(r);
        }
    }

    public List<ModelResult> getPhysicalLastN(int n) {
        synchronized(physicalLogs) {
            int size = physicalLogs.size();
            return new ArrayList<>(physicalLogs.subList(Math.max(0, size - n), size));
        }
    }

    public List<ModelResult> getSimLogs() {
        return new ArrayList<>(simLogs);
    }

    public List<ModelResult> getSimHistoryForLamp(String lampId) {
        synchronized(simLogs) {
            List<ModelResult> result = new ArrayList<>();
            for (ModelResult r : simLogs)
                if (lampId.equals(r.getLampId())) result.add(r);
            return result;
        }
    }

    public void clear() {
        physicalLogs.clear();
        simLogs.clear();
        counter.set(0);
    }
}

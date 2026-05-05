package com.bioreactordt.bioreactormockservice.replay;

import java.time.Duration;
import java.util.*;

class MapTimeSerie<I, V> implements TimeSerie<I, V> {

    private static class DataPoint<V> {
        Duration duration;
        V value;
    }

    private final Map<I, List<DataPoint<V>>> valueMap = new HashMap<>();

    public void addId(I id) {
        valueMap.put(id, new ArrayList<>());
    }

    /**
     * Assumes id exists and that data points are added chronologically
     */
    public void addDataPoint(I id, Duration duration, V value) {
        DataPoint<V> dataPoint = new DataPoint<>();
        dataPoint.duration = duration;
        dataPoint.value = value;
        valueMap.get(id).add(dataPoint);
    }

    @Override
    public Set<I> getIDs() {
        return valueMap.keySet();
    }

    @Override
    public V getValue(I id, Duration duration) {
        // TODO protect against out of bound accesses
        List<DataPoint<V>> dataPoints = valueMap.get(id);
        int lastIndex = 0;
        DataPoint<V> nextDataPoint = dataPoints.get(lastIndex);
        while (duration.compareTo(nextDataPoint.duration) > 0) {
            nextDataPoint = dataPoints.get(lastIndex++);
        }
        return dataPoints.get(lastIndex-2).value;
    }

}

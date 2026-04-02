package com.bioreactordt.bioreactormockservice.replay;

import java.time.Duration;
import java.util.Set;
import java.util.function.Function;

public class ModTimeSerie<I, V> implements TimeSerie<I, V> {

    private final Function<Duration, Duration> timeModifier;
    private final TimeSerie<I, V> timeSerie;

    public ModTimeSerie(TimeSerie<I, V> timeSerie, Function<Duration, Duration> timeModifier) {
        this.timeModifier = timeModifier;
        this.timeSerie = timeSerie;
    }

    @Override
    public Set<I> getIDs() {
        return timeSerie.getIDs();
    }

    @Override
    public V getValue(I id, Duration duration) {
        return timeSerie.getValue(id, timeModifier.apply(duration));
    }

}

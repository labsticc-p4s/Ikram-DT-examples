package com.bioreactordt.bioreactormockservice.replay;

import java.time.Duration;
import java.util.Set;

public interface TimeSerie<I, V> {

    Set<I> getIDs();
    V getValue(I id, Duration duration);

}

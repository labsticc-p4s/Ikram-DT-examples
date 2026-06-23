import { useState, useEffect, useCallback, useRef } from 'react';
import axios from 'axios';
import { EXP } from '../helpers/constants';

export function useStoragePanel() {
    const [experiments, setExperiments] = useState([]);
    const [loading,     setLoading]     = useState(false);
    const inFlight = useRef(new Set());

    const fetchOne = useCallback(async (experimentId) => {
        if (inFlight.current.has(experimentId)) return;
        inFlight.current.add(experimentId);
        try {
            const r = await axios.get(`${EXP}/${experimentId}`);
            const data = r.data;
            setExperiments(prev => prev.map(e =>
                e.experimentId === experimentId
                    ? {
                        ...e,
                        reactorId:       data.reactorId       ?? e.reactorId,
                        populationModel: data.populationModel ?? e.populationModel,
                        phModel:         data.phModel         ?? e.phModel,
                        tempModel:       data.tempModel       ?? e.tempModel,
                        source:          data.source != null ? data.source.toLowerCase() : e.source,
                        states:          data.states          ?? [],
                        fetchedAt:       new Date().toLocaleTimeString(),
                    }
                    : e
            ));
        } catch (_) {
            // silent
        } finally {
            inFlight.current.delete(experimentId);
        }
    }, []);

    const loadAll = useCallback(async () => {
        try {
            const r = await axios.get(EXP);
            if (!Array.isArray(r.data) || r.data.length === 0) return;
            setExperiments(r.data.map(e => ({
                ...e,
                states: [],
                fetchedAt: null,
                registeredAt: null,
            })));
            r.data.forEach(e => fetchOne(e.experimentId));
        } catch (_) {}
    }, [fetchOne]);

    // Load on mount, retry every 5s until data arrives
    useEffect(() => {
        loadAll();
        const id = setInterval(() => {
            setExperiments(prev => {
                if (prev.length === 0) loadAll();
                return prev;
            });
        }, 5000);
        return () => clearInterval(id);
    }, [loadAll]);

    const registerExperiment = useCallback((entry) => {
        if (!entry?.experimentId) return;
        setExperiments(prev => {
            if (prev.find(e => e.experimentId === entry.experimentId)) return prev;
            return [{
                ...entry,
                states: [],
                fetchedAt: null,
                registeredAt: new Date().toLocaleTimeString(),
            }, ...prev];
        });
        setTimeout(() => fetchOne(entry.experimentId), 2000);
    }, [fetchOne]);

    const refreshExperiment = useCallback(async (experimentId) => {
        setLoading(true);
        await fetchOne(experimentId);
        setLoading(false);
    }, [fetchOne]);

    const refreshAll = useCallback(async () => {
        setLoading(true);
        await Promise.all(experiments.map(e => fetchOne(e.experimentId)));
        setLoading(false);
    }, [experiments, fetchOne]);

    useEffect(() => {
        if (experiments.length === 0) return;
        const id = setInterval(() => {
            experiments.forEach(e => fetchOne(e.experimentId));
        }, 3000);
        return () => clearInterval(id);
    }, [experiments, fetchOne]);

    return { experiments, loading, registerExperiment, refreshExperiment, refreshAll };
}
import { useState, useEffect } from 'react';
import axios from 'axios';
import { P, T, S, M, ST, GW } from '../helpers/constants';

const emptyResult = { ph: 7.0, temperature: 37.0, population: 0, gammaPh: 0, gammaTemp: 0, mu: 0, growthStatus: '—' };

export function useReactor() {
    const [phys,               setPhys]              = useState({ reactorId: 'BIOREACTOR-001', ph: 7.0, temperature: 37.0, hours: 0 });
    const [speedFactor,        setSpeedFactorState]  = useState(30);
    const [physStrains,        setPhysStrains]       = useState([]);
    const [selectedStrainIds,  setSelectedStrainIds] = useState([]);
    const [twin,               setTwin]              = useState(emptyResult);
    const [synced,             setSynced]            = useState(false);
    const [history,            setHistory]           = useState([]);
    const [availableModels,    setAvailableModels]   = useState({ ph: [], temperature: [], population: [] });
    const [modelSelection,     setModelSelection]    = useState({ phModel: 'cardinal', temperatureModel: 'cardinal', populationModel: 'logistic' });

    const fetchPhys = async () => {
        try { const r = await axios.get(`${P}/state`); setPhys(r.data); } catch (_) {}
    };

    const fetchPhysStrains = async () => {
        try { const r = await axios.get(`${ST}/reactor/BIOREACTOR-001`); setPhysStrains(r.data || []); } catch (_) {}
    };

    const fetchAll = async () => {
        try {
            const [t, ph, sy] = await Promise.allSettled([
                axios.get(`${T}/state`),
                axios.get(`${S}/physical/last/50`),
                axios.get(`${T}/sync`),
            ]);
            if (t.status  === 'fulfilled' && t.value.data) setTwin(t.value.data);
            if (ph.status === 'fulfilled')                  setHistory([...ph.value.data].reverse());
            if (sy.status === 'fulfilled') {
                const d = sy.value.data;
                setSynced(d.twinned ?? false);
                if (d.strainIds?.length) setSelectedStrainIds(d.strainIds);
            }
        } catch (_) {}
    };

    useEffect(() => {
        axios.get(`${P}/speed`).then(r => setSpeedFactorState(r.data.speedFactor || 30)).catch(() => {});
        axios.get(`${M}/available`).then(r => setAvailableModels(r.data)).catch(() => {});
        axios.get(`${M}/BIOREACTOR-001`).then(r => setModelSelection({
            phModel:          r.data.phModel,
            temperatureModel: r.data.temperatureModel,
            populationModel:  r.data.populationModel,
        })).catch(() => {});
        fetchPhysStrains();
        fetchAll();
        fetchPhys();
        const a = setInterval(fetchAll,         1000);
        const b = setInterval(fetchPhys,        1000);
        const c = setInterval(fetchPhysStrains, 5000);
        return () => { clearInterval(a); clearInterval(b); clearInterval(c); };
    }, []);

    const setSpeed = v => { setSpeedFactorState(v); axios.post(`${P}/speed`, { speedFactor: v }); };

    const toggleStrainSelection = id =>
        setSelectedStrainIds(prev => prev.includes(id) ? prev.filter(s => s !== id) : [...prev, id]);

    const applyModelSelection = () =>
        axios.post(`${M}/select`, { reactorId: 'BIOREACTOR-001', ...modelSelection });

    const toggleSync = async () => {
        const next = !synced;
        if (next) {
            await Promise.all([
                axios.post(`${T}/sync/enable`, { reactorId: 'BIOREACTOR-001', strainIds: selectedStrainIds }),
                axios.post(`${S}/sync/enable`, null, { params: { reactorId: 'BIOREACTOR-001' } }),
            ]);
        } else {
            await Promise.all([axios.post(`${T}/sync/disable`), axios.post(`${S}/sync/disable`)]);
        }
        setSynced(next);
    };

    return {
        phys, speedFactor, setSpeed,
        physStrains, fetchPhysStrains,
        selectedStrainIds, toggleStrainSelection,
        twin, synced, toggleSync,
        history,
        availableModels, modelSelection, setModelSelection, applyModelSelection,
    };
}
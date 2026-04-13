import { useState, useEffect } from 'react';
import axios from 'axios';
import { T, S, ST, defaultSimStrain, toHours, toSeconds, PER_PAGE } from  '../helpers/constants';

export function useSimulations() {
    const [sims,        setSims]        = useState([]);
    const [simHistory,  setSimHistory]  = useState([]);
    const [simForm,     setSimForm]     = useState({ description: '', stepIntervalMs: 500 });
    const [simVal,      setSimVal]      = useState(60);
    const [simUnit,     setSimUnit]     = useState('s');
    const [steps,       setSteps]       = useState([{ ph: 7.0, temperature: 37.0, val: 100, unit: 'h', strains: [defaultSimStrain()] }]);
    const [simPage,     setSimPage]     = useState(1);
    const [selectedSim, setSelectedSim] = useState(null);

    const fetchSims = async () => {
        try {
            const [sh, sm] = await Promise.allSettled([
                axios.get(`${S}/simulations`),
                axios.get(`${T}/simulations`),
            ]);
            if (sh.status === 'fulfilled') setSimHistory(sh.value.data || []);
            if (sm.status === 'fulfilled') setSims(sm.value.data || []);
        } catch (_) {}
    };

    useEffect(() => {
        fetchSims();
        const id = setInterval(fetchSims, 1000);
        return () => clearInterval(id);
    }, []);

    const launchSim = async () => {
        const simDurationSeconds = toSeconds(simVal, simUnit);
        const mappedSteps = await Promise.all(steps.map(async (s, i) => {
            const strainIds = await Promise.all(s.strains.map(async st => {
                const simReactorId = `SIM-${Date.now()}-step${i}`;
                const id = `${simReactorId}-strain`;
                await axios.post(ST, { ...st, strainId: id, reactorId: simReactorId, name: st.name });
                return id;
            }));
            return { ph: s.ph, temperature: s.temperature, realDurationHours: toHours(s.val, s.unit), strainIds };
        }));
        axios.post(`${T}/simulations`, { ...simForm, simDurationSeconds, steps: mappedSteps });
    };

    const stopSim    = id => axios.delete(`${T}/simulations/${id}`);
    const pauseSim   = id => axios.post(`${T}/simulations/${id}/pause`);
    const resumeSim  = id => axios.post(`${T}/simulations/${id}/resume`);
    const stopAllSim = ()  => axios.delete(`${T}/simulations`);

    const addStep           = ()           => setSteps(p => [...p, { ph: 7.0, temperature: 37.0, val: 100, unit: 'h', strains: [defaultSimStrain()] }]);
    const removeStep        = i            => setSteps(p => p.filter((_, idx) => idx !== i));
    const updateStep        = (i, f, v)    => setSteps(p => p.map((s, idx) => idx === i ? { ...s, [f]: v } : s));
    const updateStepStrains = (i, strains) => setSteps(p => p.map((s, idx) => idx === i ? { ...s, strains } : s));

    const simIds        = [...new Set(simHistory.map(r => r.reactorId))].sort();
    const simTotalPages = Math.ceil(simIds.length / PER_PAGE);
    const simPageIds    = simIds.slice((simPage - 1) * PER_PAGE, simPage * PER_PAGE);
    const selectedRecs  = selectedSim ? simHistory.filter(r => r.reactorId === selectedSim) : [];

    return {
        sims, simHistory,
        simForm, setSimForm,
        simVal, setSimVal,
        simUnit, setSimUnit,
        steps,
        addStep, removeStep, updateStep, updateStepStrains,
        launchSim, stopSim, pauseSim, resumeSim, stopAllSim,
        simPage, setSimPage, simTotalPages, simPageIds,
        selectedSim, setSelectedSim, selectedRecs,
    };
}
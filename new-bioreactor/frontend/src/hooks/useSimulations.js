import { useState } from 'react';
import axios from 'axios';
import { SIM, defaultSimStep } from '../helpers/constants';

const defaultForm = () => ({
    reactorId: 'BIOREACTOR-001',
    condInit: '',
    totalScreenMin: 2,
    ticksPerStep: 10,
    populationModel: 'logistic',
    phModel: 'cardinal',
    tempModel: 'cardinal',
});

export function useSimulations() {
    const [form,        setForm]        = useState(defaultForm());
    const [steps,       setSteps]       = useState([defaultSimStep()]);
    const [launching,   setLaunching]   = useState(false);
    const [flash,       setFlash]       = useState(null);
    const [lastSimId,   setLastSimId]   = useState(null);

    const showFlash = (msg, ok = true) => {
        setFlash({ msg, ok });
        setTimeout(() => setFlash(null), 4000);
    };

    const addStep    = ()        => setSteps(p => [...p, defaultSimStep()]);
    const removeStep = i         => setSteps(p => p.filter((_, idx) => idx !== i));
    const updateStep = (i, f, v) => setSteps(p => p.map((s, idx) => idx === i ? { ...s, [f]: v } : s));

    const launchSim = async (onLaunched) => {
        if (!form.condInit.trim()) { showFlash('condInit is required', false); return; }
        setLaunching(true);
        try {
            const res = await axios.post(`${SIM}/start`, {
                ...form,
                steps: steps.map(s => ({
                    ph: s.ph,
                    temperature: s.temperature,
                    realDurationMin: s.realDurationMin,
                })),
            });

            const text = typeof res.data === 'string' ? res.data : '';
            const match = text.match(/Simulation started with (.+)/);
            const expId = match ? match[1].trim() : null;

            if (expId) {
                setLastSimId(expId);
                showFlash(`Simulation started! ID: ${expId.slice(0, 8)}…`);
                if (onLaunched) onLaunched({
                    experimentId: expId,
                    source: 'sim',
                    reactorId: form.reactorId,
                    condInit: form.condInit,
                    populationModel: form.populationModel,
                    phModel: form.phModel,
                    tempModel: form.tempModel,
                });
            } else {
                showFlash('Simulation started!');
                if (onLaunched) onLaunched(null);
            }
        } catch (err) {
            const msg = err?.response?.data || err?.message || 'unknown error';
            showFlash(`Failed: ${JSON.stringify(msg)}`, false);
        } finally {
            setLaunching(false);
        }
    };

    return {
        form, setForm,
        steps, addStep, removeStep, updateStep,
        launching, flash,
        launchSim,
        lastSimId,
    };
}
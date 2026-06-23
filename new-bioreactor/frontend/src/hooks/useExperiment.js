import { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { GW, EXP } from '../helpers/constants';

const defaultForm = () => ({
    reactorId: 'BIOREACTOR-001',
    condInit: '',
    populationModel: 'logistic',
    phModel: 'cardinal',
    tempModel: 'cardinal',
});

const emptyTwin = {
    mu: undefined, population: 0,
    gammaPh: 0, gammaTemp: 0,
    growthStatus: '—', ph: 0, temperature: 0,
};

// Stop polling after this many consecutive empty/error responses
const MAX_ERRORS = 5;

export function useExperiment() {
    const [form,        setForm]        = useState(defaultForm());
    const [twinActive,  setTwinActive]  = useState(false);
    const [loading,     setLoading]     = useState(false);
    const [flash,       setFlash]       = useState(null);
    const [activeExpId, setActiveExpId] = useState(null);
    const [twin,        setTwin]        = useState(emptyTwin);
    const pollRef    = useRef(null);
    const errorCount = useRef(0);

    const showFlash = (msg, ok = true) => {
        setFlash({ msg, ok });
        setTimeout(() => setFlash(null), 3500);
    };

    const fetchTwinState = async (experimentId) => {
        if (!experimentId) return;
        try {
            // FIX 3.1: call the lightweight last-state endpoint during live polling
            // instead of fetching the full history array on every tick.
            const r = await axios.get(`${EXP}/${experimentId}/last-state`);
            const state = r.data;
            if (state && state.population !== undefined) {
                errorCount.current = 0;
                setTwin(state);
            } else {
                errorCount.current += 1;
                if (errorCount.current >= MAX_ERRORS) {
                    stopPoll();
                    setTwinActive(false);
                    setActiveExpId(null);
                    setTwin(emptyTwin);
                    showFlash('Twin lost — GreyCat was restarted. Please start a new session.', false);
                }
            }
        } catch (_) {
            errorCount.current += 1;
            if (errorCount.current >= MAX_ERRORS) {
                stopPoll();
                setTwinActive(false);
                setActiveExpId(null);
                setTwin(emptyTwin);
                showFlash('Cannot reach experiment service. Twin stopped.', false);
            }
        }
    };

    const startPoll = (experimentId) => {
        stopPoll();
        errorCount.current = 0;
        fetchTwinState(experimentId);
        pollRef.current = setInterval(() => fetchTwinState(experimentId), 1000);
    };

    const stopPoll = () => {
        if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null; }
    };

    useEffect(() => () => stopPoll(), []);

    const startTwin = async (onStarted) => {
        if (!form.condInit.trim()) { showFlash('condInit is required', false); return; }
        setLoading(true);
        try {
            const res   = await axios.post(`${GW}/start`, form);
            const expId = res.data?.experimentId ?? null;
            setTwinActive(true);
            setActiveExpId(expId);
            setTwin(emptyTwin);
            showFlash(expId ? `Twin started! ID: ${expId.slice(0, 8)}…` : 'Twin started!');
            if (expId) startPoll(expId);
            if (onStarted) onStarted({
                experimentId:    expId,
                source:          'physical',
                reactorId:       form.reactorId,
                condInit:        form.condInit,
                populationModel: form.populationModel,
                phModel:         form.phModel,
                tempModel:       form.tempModel,
            });
        } catch (_) {
            showFlash('Failed to start twin', false);
        } finally {
            setLoading(false);
        }
    };

    const stopTwin = async () => {
        setLoading(true);
        stopPoll();
        try {
            await axios.post(`${GW}/stop`, { reactorId: form.reactorId });
        } catch (_) {}
        setTwinActive(false);
        setActiveExpId(null);
        setTwin(emptyTwin);
        showFlash('Twin stopped.');
        setLoading(false);
    };

    return {
        form, setForm,
        twinActive, loading, flash,
        activeExpId,
        twin,
        startTwin, stopTwin,
    };
}
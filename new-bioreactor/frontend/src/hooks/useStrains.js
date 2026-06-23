import { useState, useEffect } from 'react';
import axios from 'axios';
import { ST } from '../helpers/constants';

const defaultFamily = () => ({
    strainId: `FAMILY-${Date.now()}`,
    name: 'New Strain',
    muMax: 0.8,
    latency: 0.01,
    phMin: 5.0, phOpt: 7.0, phMax: 9.0,
    tempMin: 25.0, tempOpt: 37.0, tempMax: 45.0,
});

const defaultInitial = () => ({
    condId: `COND-${Date.now()}`,
    populationInit: 1_000_000,
    populationMax: 1e10,
    familyIds: [],
});

export function useStrains() {
    const [families,  setFamilies]  = useState([]);
    const [initials,  setInitials]  = useState([]);
    const [famDraft,  setFamDraft]  = useState(defaultFamily());
    const [initDraft, setInitDraft] = useState(defaultInitial());
    const [saving,    setSaving]    = useState(false);
    const [flash,     setFlash]     = useState(null);

    useEffect(() => {
        axios.get(`${ST}/family`)
            .then(r => setFamilies(r.data))
            .catch(() => {});
        axios.get(`${ST}/initial`)
            .then(r => setInitials(r.data))
            .catch(() => {});
    }, []);

    const showFlash = (msg, ok = true) => {
        setFlash({ msg, ok });
        setTimeout(() => setFlash(null), 2500);
    };

    const saveFamily = async () => {
        setSaving(true);
        try {
            await axios.post(`${ST}/family`, famDraft);
            setFamilies(prev => {
                const exists = prev.find(f => f.strainId === famDraft.strainId);
                return exists
                    ? prev.map(f => f.strainId === famDraft.strainId ? { ...famDraft } : f)
                    : [...prev, { ...famDraft }];
            });
            showFlash('Strain family saved!');
            setFamDraft(defaultFamily());
        } catch (_) { showFlash('Failed to save family', false); }
        finally { setSaving(false); }
    };

    const saveInitial = async () => {
        setSaving(true);
        try {
            await axios.post(`${ST}/initial`, initDraft);
            setInitials(prev => {
                const exists = prev.find(c => c.condId === initDraft.condId);
                return exists
                    ? prev.map(c => c.condId === initDraft.condId ? { ...initDraft } : c)
                    : [...prev, { ...initDraft }];
            });
            showFlash('Condition (condInit) saved!');
            setInitDraft(defaultInitial());
        } catch (_) { showFlash('Failed to save condition', false); }
        finally { setSaving(false); }
    };

    const fetchInitial = async (condId) => {
        try {
            const r = await axios.get(`${ST}/initial/${condId}`);
            return r.data;
        } catch (_) { return null; }
    };

    return {
        families, initials,
        famDraft,  setFamDraft,
        initDraft, setInitDraft,
        saving, flash,
        saveFamily, saveInitial, fetchInitial,
    };
}
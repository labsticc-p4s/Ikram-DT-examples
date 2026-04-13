import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ST, invalidateCache } from '../helpers/constants';
import { StrainEditor, StrainSummary } from './StrainEditor';

export default function StrainManagementPanel({ physStrains, onStrainsChanged }) {
    const [open,   setOpen]   = useState({});
    const [saving, setSaving] = useState({});
    const [drafts, setDrafts] = useState({});

    useEffect(() => {
        setDrafts(prev => {
            const next = { ...prev };
            physStrains.forEach(s => {
                if (!next[s.strainId]) next[s.strainId] = { ...s };
            });
            return next;
        });
    }, [physStrains]);

    const toggleOpen  = id => setOpen(o => ({ ...o, [id]: !o[id] }));
    const updateDraft = (id, u) => setDrafts(d => ({ ...d, [id]: u }));

    const save = async id => {
        setSaving(s => ({ ...s, [id]: true }));
        try {
            await axios.post(ST, drafts[id]);
            await invalidateCache(id);
            setOpen(o => ({ ...o, [id]: false }));
            onStrainsChanged();
        } finally {
            setSaving(s => ({ ...s, [id]: false }));
        }
    };

    const add = async () => {
        const s = {
            strainId: `STRAIN-${Date.now()}`, reactorId: 'BIOREACTOR-001', name: 'New Strain',
            muMax: 0.8, populationInit: 1_000_000, populationMax: 1e10, latency: 0.01,
            phMin: 5.0, phOpt: 7.0, phMax: 9.0, tempMin: 25.0, tempOpt: 37.0, tempMax: 45.0,
        };
        await axios.post(ST, s);
        await invalidateCache(s.strainId);
        onStrainsChanged();
        setOpen(o => ({ ...o, [s.strainId]: true }));
    };

    const remove = async id => {
        await axios.delete(`${ST}/${id}`);
        // also drop the draft so it doesn't linger
        setDrafts(d => { const n = { ...d }; delete n[id]; return n; });
        await invalidateCache(id);
        onStrainsChanged();
    };

    return (
        <div className="env-section">
            <div className="env-title">Registered strains</div>
            {physStrains.length === 0 && (
                <p style={{ fontSize: 11, color: '#aaa', margin: '4px 0' }}>No strains registered</p>
            )}
            {physStrains.map(s => {
                const draft = drafts[s.strainId] || s;
                return (
                    <div key={s.strainId} style={{ border: '1px solid #e0e0e0', borderRadius: 6, padding: '6px 8px', marginBottom: 6 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontSize: 12, fontWeight: 600 }}>
                                {draft.name || s.strainId}
                            </span>
                            <div style={{ display: 'flex', gap: 4 }}>
                                <button className="btn-sm blue" onClick={() => toggleOpen(s.strainId)}>
                                    {open[s.strainId] ? 'Hide' : 'Edit'}
                                </button>
                                {open[s.strainId] && (
                                    <button className="btn-sm green" onClick={() => save(s.strainId)} disabled={saving[s.strainId]}>
                                        {saving[s.strainId] ? '…' : 'Save'}
                                    </button>
                                )}
                                <button className="btn-sm red" onClick={() => remove(s.strainId)}>✕</button>
                            </div>
                        </div>
                        {open[s.strainId]
                            ? <StrainEditor strain={draft} onChange={u => updateDraft(s.strainId, u)} />
                            : <StrainSummary strain={draft} />}
                    </div>
                );
            })}
            <button className="btn-sm blue" style={{ width: '100%', marginTop: 4 }} onClick={add}>
                + Add strain
            </button>
        </div>
    );
}

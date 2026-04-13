import React from 'react';
import { defaultSimStrain } from '../helpers/constants';
import { StrainEditor, StrainSummary } from './StrainEditor';

export default function SimStrainList({ strains, onChange }) {
    const add    = () => onChange([...strains, defaultSimStrain()]);
    const remove = i  => onChange(strains.filter((_, idx) => idx !== i));
    const toggle = i  => onChange(strains.map((s, idx) => idx === i ? { ...s, open: !s.open } : s));
    const update = (i, u) => onChange(strains.map((s, idx) => idx === i ? { ...s, ...u } : s));

    return (
        <div>
            {strains.map((s, i) => (
                <div key={i} style={{ border: '1px solid #e0e0e0', borderRadius: 6, padding: '6px 8px', marginBottom: 6 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ fontSize: 12, fontWeight: 600 }}>{s.name || `Strain ${i + 1}`}</span>
                        <div style={{ display: 'flex', gap: 4 }}>
                            <button className="btn-sm blue" onClick={() => toggle(i)}>
                                {s.open ? 'Hide' : 'Edit'}
                            </button>
                            {strains.length > 1 &&
                                <button className="btn-sm red" onClick={() => remove(i)}>✕</button>}
                        </div>
                    </div>
                    {s.open
                        ? <StrainEditor strain={s} onChange={u => update(i, u)} />
                        : <StrainSummary strain={s} />}
                </div>
            ))}
            <button className="btn-sm blue" style={{ width: '100%', marginTop: 4 }} onClick={add}>
                + Add strain
            </button>
        </div>
    );
}

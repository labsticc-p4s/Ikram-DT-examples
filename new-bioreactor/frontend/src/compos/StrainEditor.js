import React from 'react';

export function FamilyEditor({ family, onChange }) {
    const sl = (field, min, max, step, label, unit = '') => (
        <div className="step-field">
            <label>{label}: <strong>{family[field]}{unit}</strong></label>
            <input type="range" min={min} max={max} step={step} value={family[field]}
                   onChange={e => onChange({ ...family, [field]: +e.target.value })} />
        </div>
    );

    return (
        <div className="strain-form">
            <div className="strain-section-title">Identity</div>
            <div className="step-field">
                <label>Name</label>
                <input type="text" value={family.name || ''}
                       placeholder="Strain name"
                       style={{ width: '100%', padding: '4px 8px', border: '1px solid #cbd5e1', borderRadius: 4, fontSize: 12, color: '#1e293b' }}
                       onChange={e => onChange({ ...family, name: e.target.value })} />
            </div>

            <div className="strain-section-title">Growth rate</div>
            {sl('muMax', 0.1, 3.0, 0.05, 'µ max', ' h⁻¹')}
            {sl('latency', 0, 5, 0.01, 'Lag phase', ' h')}

            <div className="strain-section-title">pH tolerance</div>
            {sl('phMin', 0, 14, 0.1, 'pH min')}
            {sl('phOpt', 0, 14, 0.1, 'pH opt')}
            {sl('phMax', 0, 14, 0.1, 'pH max')}

            <div className="strain-section-title">Temperature tolerance</div>
            {sl('tempMin', 0, 60, 0.5, 'Temp min', ' °C')}
            {sl('tempOpt', 0, 60, 0.5, 'Temp opt', ' °C')}
            {sl('tempMax', 0, 60, 0.5, 'Temp max', ' °C')}
        </div>
    );
}

export function FamilySummary({ family }) {
    return (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginTop: 4 }}>
            <span style={chipStyle('#eff6ff', '#1d4ed8')}>
                µ {family.muMax} h⁻¹
            </span>
            <span style={chipStyle('#f0fdf4', '#15803d')}>
                pH {family.phMin} | <strong>{family.phOpt}</strong> | {family.phMax}
            </span>
            <span style={chipStyle('#fff7ed', '#c2410c')}>
                T {family.tempMin} | <strong>{family.tempOpt}</strong> | {family.tempMax}°C
            </span>
        </div>
    );
}

const chipStyle = (bg, color) => ({
    display: 'inline-flex',
    alignItems: 'center',
    fontSize: 10,
    fontWeight: 600,
    background: bg,
    color,
    border: `1px solid ${color}22`,
    borderRadius: 20,
    padding: '2px 7px',
    whiteSpace: 'nowrap',
});

export function InitialStrainEditor({ strain, families, onChange }) {
    const toggle = id =>
        onChange({
            ...strain,
            familyIds: strain.familyIds.includes(id)
                ? strain.familyIds.filter(f => f !== id)
                : [...strain.familyIds, id],
        });

    return (
        <div className="strain-form">
            <div className="strain-section-title">Population</div>
            <div className="step-field">
                <label>Initial N₀ (cells/mL)</label>
                <input type="number" step="any" value={strain.populationInit}
                       style={{ width: '100%', padding: '4px 8px', border: '1px solid #cbd5e1', borderRadius: 4, fontSize: 12, color: '#1e293b' }}
                       onChange={e => onChange({ ...strain, populationInit: +e.target.value })} />
            </div>
            <div className="step-field">
                <label>Max Nmax (cells/mL)</label>
                <input type="number" step="any" value={strain.populationMax}
                       style={{ width: '100%', padding: '4px 8px', border: '1px solid #cbd5e1', borderRadius: 4, fontSize: 12, color: '#1e293b' }}
                       onChange={e => onChange({ ...strain, populationMax: +e.target.value })} />
            </div>

            <div className="strain-section-title">Associated families</div>
            {families.length === 0
                ? <p style={{ fontSize: 11, color: '#aaa' }}>No families registered yet.</p>
                : families.map(f => (
                    <label key={f.strainId} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12, marginBottom: 4, cursor: 'pointer', color: '#1e293b' }}>
                        <input type="checkbox"
                               checked={strain.familyIds.includes(f.strainId)}
                               onChange={() => toggle(f.strainId)} />
                        <span><strong>{f.name || f.strainId}</strong> — µmax={f.muMax}</span>
                    </label>
                ))
            }
        </div>
    );
}
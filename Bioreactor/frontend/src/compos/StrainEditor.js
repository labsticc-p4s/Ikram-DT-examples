import React from 'react';

export function StrainSummary({ strain }) {
    return (
        <div style={{ fontSize: 11, color: '#888', marginTop: 2 }}>
            µmax={strain.muMax} · pH [{strain.phMin}–{strain.phMax}] · T [{strain.tempMin}–{strain.tempMax}°C]
        </div>
    );
}

export function StrainEditor({ strain, onChange }) {
    const sl = (field, min, max, step, label, unit) => (
        <div className="step-field">
            <label>{label}: <strong>{strain[field]}{unit}</strong></label>
            <input type="range" min={min} max={max} step={step} value={strain[field]}
                   onChange={e => onChange({ ...strain, [field]: +e.target.value })} />
        </div>
    );
    const num = (field, label) => (
        <div className="step-field">
            <label>{label}</label>
            <input type="number" step="any" value={strain[field]}
                   style={{ width: '100%', padding: '4px 8px', border: '1px solid #ddd', borderRadius: 4, fontSize: 12 }}
                   onChange={e => onChange({ ...strain, [field]: +e.target.value })} />
        </div>
    );
    return (
        <div className="strain-form">
            <div className="strain-section-title">Name</div>
            <div className="step-field">
                <input
                    type="text"
                    value={strain.name || ''}
                    placeholder="Strain name"
                    style={{ width: '100%', padding: '4px 8px', border: '1px solid #ddd', borderRadius: 4, fontSize: 12 }}
                    onChange={e => onChange({ ...strain, name: e.target.value })}
                />
            </div>
            <div className="strain-section-title">Growth rate</div>
            {sl('muMax', 0.1, 3.0, 0.05, 'µ max', ' h⁻¹')}
            <div className="strain-section-title">pH tolerance</div>
            {sl('phMin', 0, 14, 0.1, 'pH min', '')}
            {sl('phOpt', 0, 14, 0.1, 'pH opt', '')}
            {sl('phMax', 0, 14, 0.1, 'pH max', '')}
            <div className="strain-section-title">Temperature tolerance</div>
            {sl('tempMin', 0, 60, 0.5, 'Temp min', ' °C')}
            {sl('tempOpt', 0, 60, 0.5, 'Temp opt', ' °C')}
            {sl('tempMax', 0, 60, 0.5, 'Temp max', ' °C')}
            <div className="strain-section-title">Population</div>
            {num('populationInit', 'Initial N₀ (cells/mL)')}
            {num('populationMax',  'Max Nmax (cells/mL)')}
            {num('latency',        'Lag phase (hours)')}
        </div>
    );
}

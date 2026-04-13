import React from 'react';
import { fmt2, fmt4, fmtPop, statusClass, badge } from  '../helpers/constants';
import GammaBar from './GammaBar';

export default function TwinCard({
    twin, synced, toggleSync,
    physStrains, selectedStrainIds, toggleStrainSelection,
    availableModels, modelSelection, setModelSelection, applyModelSelection,
}) {
    return (
        <div className="card">
            <h2>Digital Twin {synced && <span className="sync-badge">Twinned</span>}</h2>

            <div className={`reactor-circle ${statusClass(twin.growthStatus)}`}>
                <span className="reactor-label">{twin.growthStatus || '—'}</span>
            </div>
            <div className="mu-display">{fmt4(twin.mu)}</div>
            <div className="mu-label">µ: growth rate (/h)</div>
            <div className="pop-display">{fmtPop(twin.population)}</div>
            <div className="pop-label">Bact/mL</div>
            <div className="info-row" style={{ marginTop: 8 }}>
                <span>pH</span><span>{fmt2(twin.ph)}</span>
            </div>
            <div className="info-row">
                <span>Temperature</span><span>{fmt2(twin.temperature)} °C</span>
            </div>
            <div className="model-section">
                <GammaBar label="gamma pH"   value={twin.gammaPh}   />
                <GammaBar label="gamma Temp" value={twin.gammaTemp} />
                <div className="info-row"><span>Status</span>{badge(twin.growthStatus)}</div>
            </div>

            <div className="env-section">
                <div className="env-title">Model selection</div>
                {[
                    { label: 'pH model',          category: 'ph',          key: 'phModel'          },
                    { label: 'Temperature model', category: 'temperature', key: 'temperatureModel' },
                    { label: 'Population model',  category: 'population',  key: 'populationModel'  },
                ].map(({ label, category, key }) => (
                    <div key={category} style={{ marginBottom: 10 }}>
                        <div className="strain-section-title">{label}</div>
                        {(availableModels[category] || []).map(m => (
                            <label key={m} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12, marginBottom: 4, cursor: 'pointer' }}>
                                <input
                                    type="radio"
                                    name={category}
                                    value={m}
                                    checked={modelSelection[key] === m}
                                    onChange={() => setModelSelection(prev => ({ ...prev, [key]: m }))}
                                />
                                <span style={{ fontWeight: 600 }}>{m}</span>
                            </label>
                        ))}
                    </div>
                ))}
                <button className="btn blue full-width" style={{ marginTop: 4 }} onClick={applyModelSelection}>
                    Apply
                </button>
            </div>

            <div className="env-section">
                <div className="env-title">Twinning</div>
                {physStrains.length === 0
                    ? <p style={{ fontSize: 11, color: '#aaa', margin: '4px 0' }}>No strains registered.</p>
                    : <>
                        <p style={{ fontSize: 11, color: '#888', marginBottom: 6 }}>
                            Select which strains to twin with:
                        </p>
                        {physStrains.map(s => (
                            <label key={s.strainId} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12, marginBottom: 4, cursor: 'pointer' }}>
                                <input
                                    type="checkbox"
                                    checked={selectedStrainIds.includes(s.strainId)}
                                    onChange={() => toggleStrainSelection(s.strainId)}
                                    disabled={synced}
                                />
                                <span style={{ fontWeight: 600 }}>{s.name || s.strainId}</span>
                            </label>
                        ))}
                      </>
                }
                <button
                    className={`btn ${synced ? 'red' : 'green'} full-width`}
                    style={{ marginTop: 8 }}
                    disabled={!synced && selectedStrainIds.length === 0}
                    onClick={toggleSync}
                >
                    {synced
                        ? 'Untwin'
                        : `Twin with ${selectedStrainIds.length} strain${selectedStrainIds.length !== 1 ? 's' : ''}`}
                </button>
            </div>
        </div>
    );
}
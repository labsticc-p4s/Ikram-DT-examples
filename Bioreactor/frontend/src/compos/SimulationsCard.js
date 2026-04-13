import React from 'react';
import { fmt2, fmt4, fmtPop } from  '../helpers/constants';
import SimStrainList from './SimStrainList';

export default function SimulationsCard({
    sims,
    simForm, setSimForm,
    simVal, setSimVal,
    simUnit, setSimUnit,
    steps, addStep, removeStep, updateStep, updateStepStrains,
    launchSim, stopSim, pauseSim, resumeSim, stopAllSim,
}) {
    return (
        <div className="card sim-card">
            <h2>Simulations</h2>
            <div className="sim-form">
                <input type="text" placeholder="scenario title"
                       value={simForm.description}
                       onChange={e => setSimForm({ ...simForm, description: e.target.value })} />

                <div className="steps-header">
                    <span className="time-row-label" style={{ margin: 0 }}>Scenario steps</span>
                    <button className="btn-sm blue" onClick={addStep}>+ Add step</button>
                </div>

                {steps.map((s, i) => (
                    <div key={i} className="step-row">
                        <div className="step-fields">
                            <div className="step-field">
                                <label>pH <strong>{fmt2(s.ph)}</strong></label>
                                <input type="range" min="0" max="14" step="0.1" value={s.ph}
                                       onChange={e => updateStep(i, 'ph', +e.target.value)} />
                            </div>
                            <div className="step-field">
                                <label>Temperature <strong>{fmt2(s.temperature)} °C</strong></label>
                                <input type="range" min="0" max="60" step="0.5" value={s.temperature}
                                       onChange={e => updateStep(i, 'temperature', +e.target.value)} />
                            </div>
                            <div className="step-field">
                                <label>Real duration</label>
                                <div className="time-input-row" style={{ marginBottom: 0 }}>
                                    <input type="number" min="1" value={s.val}
                                           onChange={e => updateStep(i, 'val', +e.target.value)} />
                                    <select value={s.unit} onChange={e => updateStep(i, 'unit', e.target.value)}>
                                        <option value="h">h</option>
                                        <option value="min">min</option>
                                        <option value="s">s</option>
                                    </select>
                                </div>
                            </div>
                            <div className="strain-section-title" style={{ marginTop: 8 }}>Strains for this step</div>
                            <SimStrainList strains={s.strains} onChange={strains => updateStepStrains(i, strains)} />
                        </div>
                        {steps.length > 1 &&
                            <button className="btn-sm red step-remove" onClick={() => removeStep(i)}>✕</button>}
                    </div>
                ))}

                <div className="time-row-label">Simulation screen duration</div>
                <div className="time-input-row">
                    <input type="number" min="1" value={simVal} onChange={e => setSimVal(+e.target.value)} />
                    <select value={simUnit} onChange={e => setSimUnit(e.target.value)}>
                        <option value="s">seconds</option>
                        <option value="min">minutes</option>
                        <option value="h">hours</option>
                    </select>
                </div>
                <div className="form-row">
                    <div>
                        <label className="time-row-label">Update interval (ms)</label>
                        <input type="number" min="100" value={simForm.stepIntervalMs}
                               onChange={e => setSimForm({ ...simForm, stepIntervalMs: +e.target.value })} />
                    </div>
                </div>
                <div className="btn-row">
                    <button className="btn blue" onClick={launchSim}>Launch</button>
                    <button className="btn red"  onClick={stopAllSim}>Stop All</button>
                </div>
            </div>

            <div className="sim-list">
                {sims.length === 0 && <p className="empty-note">No simulations running</p>}
                {sims.map(s => {
                    const m = s.latestModelResult;
                    return (
                        <div key={s.simId} className="sim-item">
                            <div className="sim-item-header">
                                <span className="sim-id">{s.simId}</span>
                                <span className={`sim-state ${s.state?.toLowerCase()}`}>{s.state}</span>
                                <span className="timescale-badge">
                                    ×{Math.round((s.totalRealDurationHours * 3600) / s.simDurationSeconds).toLocaleString()}
                                </span>
                            </div>
                            <p className="sim-desc">{s.description}</p>
                            {s.steps && s.steps.length > 1 && (
                                <div className="step-indicators">
                                    {s.steps.map((st, i) => {
                                        const startH = s.steps.slice(0, i).reduce((a, x) => a + x.realDurationHours, 0);
                                        const active = (s.realHoursElapsed || 0) >= startH
                                                    && (s.realHoursElapsed || 0) <  startH + st.realDurationHours;
                                        return (
                                            <div key={i} className={`step-pill ${active ? 'active' : ''}`}
                                                 style={{ flex: st.realDurationHours }}>
                                                <span>{i + 1}</span>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                            <div className="progress-bar">
                                <div className="progress-fill" style={{ width: `${s.progressPercent || 0}%` }} />
                            </div>
                            <span className="progress-label">{s.progressPercent || 0}%</span>
                            {m && (
                                <div className="sim-model-row">
                                    <span>µ {fmt4(m.mu)} h⁻¹</span>
                                    <span>{fmtPop(m.population)}</span>
                                </div>
                            )}
                            <div className="sim-btns">
                                {s.state === 'RUNNING' && <button className="btn-sm blue" onClick={() => pauseSim(s.simId)}>Pause</button>}
                                {s.state === 'PAUSED'  && <button className="btn-sm blue" onClick={() => resumeSim(s.simId)}>Resume</button>}
                                <button className="btn-sm red" onClick={() => stopSim(s.simId)}>Stop</button>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
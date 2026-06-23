import React, { useState } from 'react';
import { PH_MODELS, TEMP_MODELS, POP_MODELS, defaultSimStep } from '../helpers/constants';

function toMin(value, unit) {
    if (unit === 's') return value / 60;
    if (unit === 'h') return value * 60;
    return value;
}

function fromMin(valueMin, unit) {
    if (unit === 's') return Math.round(valueMin * 60);
    if (unit === 'h') return +(valueMin / 60).toFixed(2);
    return valueMin;
}

function DurationInput({ label, valueMin, onChange }) {
    const [unit, setUnit] = useState('min');

    return (
        <div className="step-field">
            <div className="time-row-label">{label}</div>
            <div className="time-input-row">
                <input
                    type="number" min="0.1" step="any"
                    value={fromMin(valueMin, unit)}
                    onChange={e => onChange(toMin(+e.target.value, unit))}
                />
                <select value={unit} onChange={e => setUnit(e.target.value)}>
                    <option value="s">seconds</option>
                    <option value="min">minutes</option>
                    <option value="h">hours</option>
                </select>
            </div>
        </div>
    );
}

function StepRow({ step, index, total, onChange, onRemove }) {
    const sl = (field, min, max, step_, label, unit = '') => (
        <div className="step-field">
            <label>{label}: <strong>{step[field]}{unit}</strong></label>
            <input
                type="range" min={min} max={max} step={step_} value={step[field]}
                onChange={e => onChange(field, +e.target.value)}
            />
        </div>
    );

    return (
        <div className="step-card">
            <div className="step-header">
                <span>Step {index + 1} / {total}</span>
                {total > 1 && <button className="btn-remove" onClick={onRemove}>✕</button>}
            </div>
            {sl('ph', 4,  10,  0.1, 'pH')}
            {sl('temperature', 15, 55,  0.5, 'Temperature', ' °C')}
            <DurationInput
                label="Step real duration"
                valueMin={step.realDurationMin}
                onChange={v => onChange('realDurationMin', v)}
            />
        </div>
    );
}

export default function SimulationsCard({
    form, setForm,
    steps, addStep, removeStep, updateStep,
    launching, flash,
    launchSim,
    lastSimId,
    initials,
}) {
    const sel = (field, opts, label) => (
        <div className="step-field">
            <label>{label}</label>
            <select value={form[field]} onChange={e => setForm(p => ({ ...p, [field]: e.target.value }))}>
                {opts.map(o => <option key={o} value={o}>{o}</option>)}
            </select>
        </div>
    );

    return (
        <div className="card">
            <h2>Simulation</h2>

            {flash && (
                <div className={`strain-flash ${flash.ok ? 'ok' : 'err'}`}>{flash.msg}</div>
            )}

            {lastSimId && (
                <div className="sim-id-box">
                    <span className="sim-id-label">Last Experiment ID</span>
                    <span className="sim-id-value" title={lastSimId}>{lastSimId}</span>
                </div>
            )}

            {/* Live single-sim state intentionally removed — multiple sims can run in
                parallel, so a single "current sim" status has no clear meaning here.
                See StoragePanel for per-experiment state. */}

            <div className="env-section">
                <div className="env-title">Configuration</div>

                <div className="step-field">
                    <label>Reactor ID</label>
                    <input
                        type="text"
                        value={form.reactorId}
                        onChange={e => setForm(p => ({ ...p, reactorId: e.target.value }))}
                        placeholder="Bioreactor id..."
                    />
                </div>

                <div className="step-field">
                    <label>condInit</label>
                    {initials && initials.length > 0 ? (
                        <select value={form.condInit} onChange={e => setForm(p => ({ ...p, condInit: e.target.value }))}>
                            <option value="">— select —</option>
                            {initials.map(c => (
                                <option key={c.condId} value={c.condId}>{c.condId}</option>
                            ))}
                        </select>
                    ) : (
                        <input
                            type="text"
                            placeholder="Condition initial for strain..."
                            value={form.condInit}
                            onChange={e => setForm(p => ({ ...p, condInit: e.target.value }))}
                        />
                    )}
                </div>

                <DurationInput
                    label="Simulation screen duration"
                    valueMin={form.totalScreenMin}
                    onChange={v => setForm(p => ({ ...p, totalScreenMin: v }))}
                />

                <div className="step-field">
                    <label>Ticks / step: <strong>{form.ticksPerStep}</strong></label>
                    <input
                        type="range" min={1} max={50} step={1} value={form.ticksPerStep}
                        onChange={e => setForm(p => ({ ...p, ticksPerStep: +e.target.value }))}
                    />
                </div>

                {sel('populationModel', POP_MODELS, 'Population model')}
                {sel('phModel',         PH_MODELS,  'pH model')}
                {sel('tempModel',       TEMP_MODELS, 'Temp model')}
            </div>

            <div className="env-section">
                <div className="env-title" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>Steps</span>
                    <button className="btn blue small" onClick={addStep}>+ Add Step</button>
                </div>

                {steps.map((s, i) => (
                    <StepRow
                        key={i} step={s} index={i} total={steps.length}
                        onChange={(f, v) => updateStep(i, f, v)}
                        onRemove={() => removeStep(i)}
                    />
                ))}
            </div>

            <button className="btn green full-width" onClick={launchSim} disabled={launching}>
                {launching ? 'Launching…' : 'Launch Simulation'}
            </button>
        </div>
    );
}
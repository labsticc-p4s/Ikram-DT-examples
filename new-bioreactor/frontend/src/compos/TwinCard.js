import React from 'react';
import { PH_MODELS, TEMP_MODELS, POP_MODELS, fmt2, fmt4, fmtPop, statusClass, badge } from '../helpers/constants';
import GammaBar from './GammaBar';

export default function TwinCard({
    form, setForm,
    twinActive, loading, flash,
    activeExpId,
    startTwin, stopTwin,
    initials,
    twin,
}) {
    const hasTwin = twin && twin.mu !== undefined;
    const sc = hasTwin ? statusClass(twin.growthStatus) : '';

    const sel = (field, opts, label) => (
        <div className="step-field">
            <label>{label}</label>
            <select
                value={form[field]}
                onChange={e => setForm(p => ({ ...p, [field]: e.target.value }))}
                disabled={twinActive}
            >
                {opts.map(o => <option key={o} value={o}>{o}</option>)}
            </select>
        </div>
    );

    return (
        <div className="card">
            <h2>
                Digital Twin
                {twinActive && <span className="twin-live-pill">LIVE</span>}
            </h2>

            {flash && (
                <div className={`strain-flash ${flash.ok ? 'ok' : 'err'}`}>{flash.msg}</div>
            )}


            {/* Reactor circle */}
            <div className={`reactor-circle ${sc}`} style={{ marginTop: 8 }}>
                <span className="reactor-label">
                    {hasTwin ? (twin.growthStatus || '—') : (twinActive ? '…' : '—')}
                </span>
            </div>

            {/* Live metrics */}
            {hasTwin ? (
                <>
                    <div className="twin-metrics-grid">
                        <div className="twin-metric">
                            <span className="twin-metric-val">{fmt4(twin.mu)}</span>
                            <span className="twin-metric-lbl">µ (/h)</span>
                        </div>
                        <div className="twin-metric">
                            <span className="twin-metric-val green">{fmtPop(twin.population)}</span>
                            <span className="twin-metric-lbl">Bact/mL</span>
                        </div>
                        <div className="twin-metric">
                            <span className="twin-metric-val">{fmt2(twin.ph)}</span>
                            <span className="twin-metric-lbl">pH</span>
                        </div>
                        <div className="twin-metric">
                            <span className="twin-metric-val">{fmt2(twin.temperature)}°C</span>
                            <span className="twin-metric-lbl">Temp</span>
                        </div>
                    </div>
                    <GammaBar label="γ pH"   value={twin.gammaPh}   />
                    <GammaBar label="γ Temp" value={twin.gammaTemp} />
                    <div className="info-row"><span>Status</span>{badge(twin.growthStatus)}</div>
                </>
            ) : (
                <p className="empty-note" style={{ margin: '6px 0' }}>
                    {twinActive ? '…' : 'Start the twinning'}
                </p>
            )}

            {/* Configuration */}
            <div className="env-section">
                <div className="env-title">Configuration</div>

                <div className="step-field">
                    <label>Reactor ID</label>
                    <input
                        type="text"
                        value={form.reactorId}
                        disabled={twinActive}
                        onChange={e => setForm(p => ({ ...p, reactorId: e.target.value }))}
                        placeholder="e.g. BIOREACTOR-001"
                    />
                </div>

                <div className="step-field">
                    <label>condInit</label>
                    {initials && initials.length > 0 ? (
                        <select
                            value={form.condInit}
                            disabled={twinActive}
                            onChange={e => setForm(p => ({ ...p, condInit: e.target.value }))}
                        >
                            <option value="">— select —</option>
                            {initials.map(c => (
                                <option key={c.condId} value={c.condId}>{c.condId}</option>
                            ))}
                        </select>
                    ) : (
                        <input
                            type="text"
                            placeholder="e.g. COND-001"
                            value={form.condInit}
                            disabled={twinActive}
                            onChange={e => setForm(p => ({ ...p, condInit: e.target.value }))}
                        />
                    )}
                </div>

                {sel('populationModel', POP_MODELS, 'Population model')}
                {sel('phModel',         PH_MODELS,  'pH model')}
                {sel('tempModel',       TEMP_MODELS, 'Temp model')}
            </div>

            {!twinActive ? (
                <button className="btn blue full-width" onClick={startTwin} disabled={loading}>
                    {loading ? 'Starting…' : '▶ Start Twin'}
                </button>
            ) : (
                <button className="btn red full-width" onClick={stopTwin} disabled={loading}>
                    {loading ? 'Stopping…' : '■ Stop Twin'}
                </button>
            )}
        </div>
    );
}
import React, { useState } from 'react';
import { fmt2, fmt4, fmtPop, badge, PER_PAGE } from '../helpers/constants';

/* ─── Sim detail popup ─────────────────────────────────── */
function SimPopup({ exp, onClose }) {
    if (!exp) return null;
    const states = exp.states ?? [];
    return (
        <div className="popup-overlay" onClick={onClose}>
            <div className="popup" onClick={e => e.stopPropagation()}>
                <div className="popup-header">
                    <h3>Simulation — <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{exp.experimentId}</span></h3>
                    <button className="popup-close" onClick={onClose}>✕</button>
                </div>
                <div className="popup-body">
                    {states.length === 0 ? (
                        <p className="empty-note">No states yet</p>
                    ) : (
                        <table className="storage-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>pH</th><th>Temp °C</th><th>Population</th>
                                    <th>γ pH</th><th>γ Temp</th><th>µ (/h)</th><th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {states.map((s, i) => (
                                    <tr key={i}>
                                        <td className="cell-muted">{i + 1}</td>
                                        <td>{fmt2(s.ph)}</td>
                                        <td>{fmt2(s.temperature)}</td>
                                        <td>{fmtPop(s.population)}</td>
                                        <td>{fmt2(s.gammaPh)}</td>
                                        <td>{fmt2(s.gammaTemp)}</td>
                                        <td className="cell-mono">{fmt4(s.mu)}</td>
                                        <td>{badge(s.growthStatus)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}

/* ─── Physical History ─────────────────────────────────── */
function PhysicalHistory({ experiments }) {
    const [page, setPage] = useState(1);

    const rows = experiments
        .filter(e => e.source === 'physical')
        .flatMap(e => (e.states ?? []).map(s => ({
            ...s,
            experimentId: e.experimentId,
            reactorId:    e.reactorId,
        })))
        .reverse();

    const totalPages = Math.max(1, Math.ceil(rows.length / PER_PAGE));
    const pageRows   = rows.slice((page - 1) * PER_PAGE, page * PER_PAGE);

    return (
        <div className="storage-section">
            <div className="storage-section-hd phys">
                Physical History
            </div>

            {rows.length === 0 ? (
                <p className="empty-note" style={{ padding: '16px 0' }}>No physical experiment states yet</p>
            ) : (
                <>
                    <div className="table-wrap">
                        <table className="storage-table">
                            <thead>
                                <tr>
                                    <th>Exp ID</th>
                                    <th>pH</th><th>Temp °C</th>
                                    <th>Population</th>
                                    <th>γ pH</th><th>γ Temp</th>
                                    <th>µ (/h)</th><th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pageRows.map((r, i) => (
                                    <tr key={i}>
                                        <td className="cell-id" title={r.experimentId}>
                                            {r.experimentId?.slice(0, 8)}…
                                        </td>
                                        <td>{fmt2(r.ph)}</td>
                                        <td>{fmt2(r.temperature)}</td>
                                        <td>{fmtPop(r.population)}</td>
                                        <td>{fmt2(r.gammaPh)}</td>
                                        <td>{fmt2(r.gammaTemp)}</td>
                                        <td className="cell-mono">{fmt4(r.mu)}</td>
                                        <td>{badge(r.growthStatus)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <div className="pagination">
                        <button className="btn-page" disabled={page === 1}         onClick={() => setPage(p => p - 1)}>Prev</button>
                        <span>Page {page} / {totalPages}</span>
                        <button className="btn-page" disabled={page >= totalPages} onClick={() => setPage(p => p + 1)}>Next</button>
                    </div>
                </>
            )}
        </div>
    );
}

/* ─── Simulation Results ───────────────────────────────── */
function SimResults({ experiments }) {
    const [simPage,     setSimPage]     = useState(1);
    const [selectedExp, setSelectedExp] = useState(null);

    const simExps    = experiments.filter(e => e.source !== 'physical');
    const totalPages = Math.max(1, Math.ceil(simExps.length / PER_PAGE));
    const pageExps   = simExps.slice((simPage - 1) * PER_PAGE, simPage * PER_PAGE);

    return (
        <div className="storage-section">
            <div className="storage-section-hd sim">
                Simulation History
            </div>

            {simExps.length === 0 ? (
                <p className="empty-note" style={{ padding: '16px 0' }}>No simulation records yet</p>
            ) : (
                <>
                    <div className="table-wrap">
                        <table className="storage-table">
                            <thead>
                                <tr>
                                    <th>Sim ID</th>
                                    <th>condInit</th>
                                    <th>States</th>
                                    <th>Last pH</th><th>Last Temp</th><th>Last µ</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pageExps.map(exp => {
                                    const states = exp.states ?? [];
                                    const last   = states.at(-1);
                                    return (
                                        <tr
                                            key={exp.experimentId}
                                            className="row-clickable"
                                            onClick={() => setSelectedExp(exp)}
                                            title="Click to view states"
                                        >
                                            <td className="cell-id">{exp.experimentId?.slice(0, 8)}…</td>
                                            <td className="cell-muted">{exp.condInit || '—'}</td>
                                            <td>{states.length}</td>
                                            <td>{fmt2(last?.ph)}</td>
                                            <td>{fmt2(last?.temperature)} °C</td>
                                            <td className="cell-mono">{fmt4(last?.mu)}</td>
                                            <td>{badge(last?.growthStatus)}</td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                    <div className="pagination">
                        <button className="btn-page" disabled={simPage === 1}         onClick={() => setSimPage(p => p - 1)}>Prev</button>
                        <span>Page {simPage} / {totalPages}</span>
                        <button className="btn-page" disabled={simPage >= totalPages} onClick={() => setSimPage(p => p + 1)}>Next</button>
                    </div>
                </>
            )}

            {selectedExp && (
                <SimPopup exp={selectedExp} onClose={() => setSelectedExp(null)} />
            )}
        </div>
    );
}

/* ─── Main panel ───────────────────────────────────────── */
export default function StoragePanel({ experiments, loading, onRefreshAll }) {
    if (experiments.length === 0) return null;

    return (
        <div className="storage-panel">
            <div className="storage-panel-header">
                <div className="storage-panel-title">
                    Experiment Storage
                </div>

            </div>

            <div className="storage-grid">
                <PhysicalHistory experiments={experiments} />
                <SimResults experiments={experiments} />
            </div>
        </div>
    );
}
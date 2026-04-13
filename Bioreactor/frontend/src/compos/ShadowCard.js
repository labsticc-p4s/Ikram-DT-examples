import React from 'react';
import { fmt2, fmt4, fmtPop, badge, PER_PAGE } from '../helpers/constants';

export default function ShadowCard({
    history, page, setPage,
    simPageIds, simTotalPages, simPage, setSimPage, simHistory,
    selectedSim, setSelectedSim, selectedRecs,
}) {
    const totalPages  = Math.ceil(history.length / PER_PAGE);
    const pageRecords = history.slice((page - 1) * PER_PAGE, page * PER_PAGE);

    return (
        <>
            <div className="shadow-section">
                <h2>Physical History</h2>
                <table className="shadow-table">
                    <thead>
                        <tr>
                            <th>Source</th><th>pH</th><th>Temp (°C)</th>
                            <th>Population</th><th>gamma pH</th><th>gamma Temp</th>
                            <th>µ (/h)</th><th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pageRecords.map((r, i) => (
                            <tr key={r.tupleId ?? i}>
                                <td className="source-cell">{r.source}</td>
                                <td>{fmt2(r.ph)}</td>
                                <td>{fmt2(r.temperature)}</td>
                                <td>{fmtPop(r.population)}</td>
                                <td>{fmt2(r.gammaPh)}</td>
                                <td>{fmt2(r.gammaTemp)}</td>
                                <td>{fmt4(r.mu)}</td>
                                <td>{badge(r.growthStatus)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                <div className="pagination">
                    <button className="btn-page" disabled={page === 1}         onClick={() => setPage(p => p - 1)}>Prev</button>
                    <span>Page {page} of {totalPages || 1}</span>
                    <button className="btn-page" disabled={page >= totalPages} onClick={() => setPage(p => p + 1)}>Next</button>
                </div>
            </div>

            <div className="sim-history-section">
                <h2>Simulation Results</h2>
                {simPageIds.length === 0
                    ? <p className="empty-note">No simulation records yet</p>
                    : <>
                        <table className="shadow-table">
                            <thead>
                                <tr>
                                    <th>Sim ID</th><th>Records</th><th>Last pH</th>
                                    <th>Last Temp</th><th>Last µ</th><th>Status</th><th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {simPageIds.map(id => {
                                    const records = simHistory.filter(r => r.reactorId === id);
                                    const last    = records[records.length - 1];
                                    return (
                                        <tr key={id} className="sim-history-row">
                                            <td><button className="sim-id-btn" onClick={() => setSelectedSim(id)}>{id}</button></td>
                                            <td>{records.length}</td>
                                            <td>{fmt2(last?.ph)}</td>
                                            <td>{fmt2(last?.temperature)} °C</td>
                                            <td>{fmt4(last?.mu)}</td>
                                            <td>{badge(last?.growthStatus)}</td>
                                            <td><button className="btn-sm blue" onClick={() => setSelectedSim(id)}>View</button></td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                        <div className="pagination">
                            <button className="btn-page" disabled={simPage === 1}            onClick={() => setSimPage(p => p - 1)}>Prev</button>
                            <span>Page {simPage} of {simTotalPages || 1}</span>
                            <button className="btn-page" disabled={simPage >= simTotalPages} onClick={() => setSimPage(p => p + 1)}>Next</button>
                        </div>
                      </>
                }
            </div>

            {selectedSim && (
                <div className="popup-overlay" onClick={() => setSelectedSim(null)}>
                    <div className="popup" onClick={e => e.stopPropagation()}>
                        <div className="popup-header">
                            <h3>{selectedSim} — Simulation detail</h3>
                            <button className="popup-close" onClick={() => setSelectedSim(null)}>✕</button>
                        </div>
                        <div className="popup-body">
                            <table className="shadow-table">
                                <thead>
                                    <tr>
                                        <th>pH</th><th>Temp (°C)</th><th>Population</th>
                                        <th>gamma pH</th><th>gamma Temp</th><th>µ (/h)</th><th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {selectedRecs.map((r, i) => (
                                        <tr key={r.tupleId ?? i}>
                                            <td>{fmt2(r.ph)}</td>
                                            <td>{fmt2(r.temperature)}</td>
                                            <td>{fmtPop(r.population)}</td>
                                            <td>{fmt2(r.gammaPh)}</td>
                                            <td>{fmt2(r.gammaTemp)}</td>
                                            <td>{fmt4(r.mu)}</td>
                                            <td>{badge(r.growthStatus)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
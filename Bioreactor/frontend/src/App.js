import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

// ── API base URLs ────────────────────────────────────────────
const P  = 'http://localhost:8081/api/physical/reactor';
const T  = 'http://localhost:8082/api/model';
const S  = 'http://localhost:8083/api/shadow';

// ── Helpers ──────────────────────────────────────────────────
const toHours   = (val, unit) => unit === 'min' ? val / 60  : unit === 's' ? val / 3600 : val;
const toSeconds = (val, unit) => unit === 'min' ? val * 60  : unit === 'h' ? val * 3600 : val;

const fmt2 = v => (v ?? 0).toFixed(2);
const fmt4 = v => (v ?? 0).toFixed(4);

// Scientific notation for large cell counts
const fmtPop = v => {
  if (!v) return '0';
  if (v >= 1e9) return (v / 1e9).toFixed(2) + ' ×10⁹';
  if (v >= 1e6) return (v / 1e6).toFixed(2) + ' ×10⁶';
  return Math.round(v).toLocaleString();
};

const statusClass = s =>
  s === 'EXPONENTIAL'  ? 'optimal'    :
  s === 'ACCELERATION' ? 'optimal'    :
  s === 'DECELERATION' ? 'suboptimal' :
  s === 'LAG'          ? 'suboptimal' :
  s === 'STATIONARY'   ? 'inhibited'  : '';

const badge = val => {
  if (!val || val === '—') return <span className="model-badge">—</span>;
  const c =
    val === 'EXPONENTIAL'  ? 'green'  :
    val === 'ACCELERATION' ? 'green'  :
    val === 'DECELERATION' ? 'orange' :
    val === 'LAG'          ? 'orange' :
    val === 'STATIONARY'   ? 'red'    : '';
  return <span className={`model-badge ${c}`}>{val}</span>;
};

// Gamma progress bar
const GammaBar = ({ label, value }) => {
  const pct   = Math.round((value ?? 0) * 100);
  const cls   = pct >= 80 ? '' : pct >= 40 ? 'suboptimal' : 'inhibited';
  return (
    <div className="gamma-bar-wrap">
      <label><span>{label}</span><span>{fmt2(value)}</span></label>
      <div className="gamma-bar">
        <div className={`gamma-fill ${cls}`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
};

const emptyResult = {
  ph: 7.0, temperature: 37.0, population: 0,
  gammaPh: 0, gammaTemp: 0, mu: 0, growthStatus: '—'
};

const PER_PAGE = 7;

// ── App ──────────────────────────────────────────────────────
export default function App() {

  // Physical state
  const [phys,     setPhys]    = useState({ reactorId: 'BIOREACTOR-001', ph: 7.0, temperature: 37.0, population: 0 });
  const [phInput,  setPhInput] = useState(7.0);
  const [tmpInput, setTmpInput] = useState(37.0);

  // Twin / shadow
  const [twin,       setTwin]       = useState(emptyResult);
  const [synced,     setSynced]     = useState(false);
  const [history,    setHistory]    = useState([]);
  const [simHistory, setSimHistory] = useState([]);

  // Simulations
  const [sims,       setSims]       = useState([]);
  const [simForm,    setSimForm]    = useState({ description: '', stepIntervalMs: 500 });
  const [simVal,     setSimVal]     = useState(60);
  const [simUnit,    setSimUnit]    = useState('s');
  const [steps,      setSteps]      = useState([{ ph: 7.0, temperature: 37.0, val: 100, unit: 'h' }]);

  // UI
  const [page,        setPage]       = useState(1);
  const [simPage,     setSimPage]    = useState(1);
  const [selectedSim, setSelectedSim] = useState(null);
  const dragging = React.useRef(false);

  // ── Fetch ──────────────────────────────────────────────────
  const fetchPhys = async () => {
    if (dragging.current) return;
    try {
      const r = await axios.get(`${P}/state`);
      setPhys(r.data);
    } catch (_) {}
  };

  const fetchAll = async () => {
    try {
      const [t, ph, sh, sm, sy] = await Promise.allSettled([
        axios.get(`${T}/state`),
        axios.get(`${S}/physical/last/50`),
        axios.get(`${S}/simulations`),
        axios.get(`${T}/simulations`),
        axios.get(`${T}/sync`),
      ]);
      if (t.status  === 'fulfilled' && t.value.data)   setTwin(t.value.data);
      if (ph.status === 'fulfilled')                    setHistory([...ph.value.data].reverse());
      if (sh.status === 'fulfilled')                    setSimHistory(sh.value.data || []);
      if (sm.status === 'fulfilled')                    setSims(sm.value.data || []);
      if (sy.status === 'fulfilled') {
        setSynced(sy.value.data.twinned ?? false);
      }
    } catch (_) {}
  };

  // ── Intervals ──────────────────────────────────────────────
  useEffect(() => {
    fetchAll();
    fetchPhys();
    const a = setInterval(fetchAll,  1000);
    const b = setInterval(fetchPhys, 1000);
    return () => { clearInterval(a); clearInterval(b); };
  }, []);

  // ── Commands ───────────────────────────────────────────────
  const setPH = v => {
    dragging.current = true;
    setPhInput(+v);
    axios.post(`${P}/env/ph`, { ph: +v })
         .finally(() => { dragging.current = false; });
  };

  const setTemp = v => {
    dragging.current = true;
    setTmpInput(+v);
    axios.post(`${P}/env/temperature`, { temperature: +v })
         .finally(() => { dragging.current = false; });
  };

  const twinCommand = () =>
    axios.post(`${T}/command`, { ph: phInput, temperature: tmpInput }).then(fetchAll);

  const toggleSync = async () => {
    const next = !synced;
    if (next) {
      await Promise.all([
        axios.post(`${T}/sync/enable`,  { reactorId: 'BIOREACTOR-001' }),
        axios.post(`${S}/sync/enable`,  null, { params: { reactorId: 'BIOREACTOR-001' } }),
      ]);
    } else {
      await Promise.all([
        axios.post(`${T}/sync/disable`),
        axios.post(`${S}/sync/disable`),
      ]);
    }
    setSynced(next);
  };

  // ── Simulation ─────────────────────────────────────────────
  const launchSim = () => {
    const simDurationSeconds = toSeconds(simVal, simUnit);
    const mappedSteps = steps.map(s => ({
      ph:               s.ph,
      temperature:      s.temperature,
      realDurationHours: toHours(s.val, s.unit),
    }));
    axios.post(`${T}/simulations`, { ...simForm, simDurationSeconds, steps: mappedSteps });
  };
  const stopSim    = id => axios.delete(`${T}/simulations/${id}`);
  const pauseSim   = id => axios.post(`${T}/simulations/${id}/pause`);
  const resumeSim  = id => axios.post(`${T}/simulations/${id}/resume`);
  const stopAllSim = ()  => axios.delete(`${T}/simulations`);
  const addStep    = ()  => setSteps(p => [...p, { ph: 7.0, temperature: 37.0, val: 100, unit: 'h' }]);
  const removeStep = i   => setSteps(p => p.filter((_, idx) => idx !== i));
  const updateStep = (i, f, v) => setSteps(p => p.map((s, idx) => idx === i ? { ...s, [f]: v } : s));

  // ── Pagination ─────────────────────────────────────────────
  const totalPages    = Math.ceil(history.length / PER_PAGE);
  const pageRecords   = history.slice((page - 1) * PER_PAGE, page * PER_PAGE);
  const simIds        = [...new Set(simHistory.map(r => r.reactorId))].sort();
  const simTotalPages = Math.ceil(simIds.length / PER_PAGE);
  const simPageIds    = simIds.slice((simPage - 1) * PER_PAGE, simPage * PER_PAGE);
  const selectedRecs  = selectedSim ? simHistory.filter(r => r.reactorId === selectedSim) : [];

  const physStatus = statusClass(phys.growthStatus ?? '');

  // ── Render ─────────────────────────────────────────────────
  return (
    <div className="app">
      <header className="app-header"><h1>Bioreactor Digital Twin</h1></header>
      <div className="top-panels">

        {/* ── PHYSICAL ── */}
        <div className="card">
          <h2>Physical Reactor</h2>

          <div className={`reactor-circle ${physStatus}`}>
            <span className="reactor-label">{phys.reactorId}</span>
          </div>

          <div className="info-row"><span>pH</span> <span>{fmt2(phys.ph)}</span></div>
          <div className="info-row"><span>Temperature</span><span>{fmt2(phys.temperature)} °C</span></div>
          <div className="info-row"><span>Population</span> <span>{fmtPop(phys.population)} bact/mL</span></div>

          <div className="env-section">
            <div className="env-title">Environment control</div>

            <div className="slider-wrap">
              <label>pH: <strong>{fmt2(phInput)}</strong></label>
              <input type="range" min="0" max="14" step="0.1" value={phInput}
                     onChange={e => setPH(e.target.value)} />
              <div style={{ display:'flex', justifyContent:'space-between', fontSize:10, color:'#aaa' }}>
                <span>0</span><span>7</span><span>14</span>
              </div>
            </div>

            <div className="slider-wrap">
              <label>Temperature: <strong>{fmt2(tmpInput)} °C</strong></label>
              <input type="range" min="0" max="60" step="0.5" value={tmpInput}
                     onChange={e => setTemp(e.target.value)} />
              <div style={{ display:'flex', justifyContent:'space-between', fontSize:10, color:'#aaa' }}>
                <span>0°C</span><span>30°C</span><span>60°C</span>
              </div>
            </div>
          </div>
        </div>

        {/* ── TWIN ── */}
        <div className="card">
          <h2>Digital Twin {synced && <span className="sync-badge">Twinned</span>}</h2>

          <div className={`reactor-circle ${statusClass(twin.growthStatus)}`}>
            <span className="reactor-label">{twin.growthStatus || '—'}</span>
          </div>

          <div className="mu-display">{fmt4(twin.mu)}</div>
          <div className="mu-label">Mu growth rate (/h)</div>

          <div className="pop-display">{fmtPop(twin.population)}</div>
          <div className="pop-label">bact/mL</div>

          <div className="info-row" style={{ marginTop: 8 }}>
            <span>pH</span><span>{fmt2(twin.ph)}</span>
          </div>
          <div className="info-row">
            <span>Temperature</span><span>{fmt2(twin.temperature)} °C</span>
          </div>

          <div className="model-section">
            <GammaBar label="gamma pH"   value={twin.gammaPh}   />
            <GammaBar label="gamma Temp" value={twin.gammaTemp} />
            <div className="info-row">
              <span>Status</span>{badge(twin.growthStatus)}
            </div>
          </div>

          <div className="twin-controls">
            <button className={`btn ${synced ? 'red' : 'green'} full-width`} onClick={toggleSync}>
              {synced ? 'Untwin' : 'Twin with BIOREACTOR-001'}
            </button>

          </div>
        </div>

        {/* ── SIMULATIONS ── */}
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
                <div className="step-number">{i + 1}</div>
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
                </div>
                {steps.length > 1 &&
                  <button className="btn-sm red step-remove" onClick={() => removeStep(i)}>✕</button>}
              </div>
            ))}

            <div className="time-row-label">Simulation screen duration</div>
            <div className="time-input-row">
              <input type="number" min="1" value={simVal}
                     onChange={e => setSimVal(+e.target.value)} />
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
              <button className="btn blue"  onClick={launchSim}>Launch</button>
              <button className="btn red"   onClick={stopAllSim}>Stop All</button>
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
                      {badge(m.growthStatus)}
                    </div>
                  )}

                  <div className="sim-btns">
                    {s.state === 'RUNNING'  && <button className="btn-sm blue" onClick={() => pauseSim(s.simId)}>Pause</button>}
                    {s.state === 'PAUSED'   && <button className="btn-sm blue" onClick={() => resumeSim(s.simId)}>Resume</button>}
                    <button className="btn-sm red" onClick={() => stopSim(s.simId)}>Stop</button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* ── BOTTOM ── */}
      <div className="bottom-section">

        {/* Shadow history */}
        <div className="shadow-section">
          <h2>Digital Shadow — Physical History</h2>
          <table className="shadow-table">
            <thead>
              <tr>
                <th>Source</th> <th>pH</th> <th>Temp (°C)</th> <th>Population</th> <th>gamma pH</th> <th>gamma Temp</th> <th>mu factor</th> <th>Status</th>
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

        {/* Sim history */}
        <div className="sim-history-section">
          <h2>Simulation Results</h2>
          {simIds.length === 0
            ? <p className="empty-note">No simulation records yet</p>
            : <>
              <table className="shadow-table">
                <thead>
                  <tr>
                    <th>Sim ID</th><th>Records</th><th>Last pH</th><th>Last Temp</th><th>Last mu</th><th>Status</th><th></th>
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
      </div>

      {/* ── POPUP ── */}
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
                    <th>gamma pH</th><th>gamma Temp</th><th>mu factor</th><th>Status</th>
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
    </div>
  );
}
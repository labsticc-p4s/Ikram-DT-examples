import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const E = (roomId) => `http://localhost:8090/api/env/room/${roomId}`;
const P = (port)   => `http://localhost:${port}/api/physical/lamp`;
const T = "http://localhost:8082/api/twin";
const S = "http://localhost:8083/api/shadow";
const R = (port)   => `http://localhost:${port}/api/physical/registry`;

const lampIdToHost = id => {
    const n = parseInt((id||'').replace(/\D/g,''), 10);
    return `physical-service-${isNaN(n)?1:n}`;
};
const lampIdToPort = id => {
    const n = parseInt((id||'').replace(/\D/g,''), 10);
    if (isNaN(n)) return 8081;
    return n === 1 ? 8081 : n === 2 ? 8091 : 8090 + n;
};

const emptyModel = { isOn:false, brightness:0, temperature:22, usageMinutes:0, roomTemp:22,
    powerWatts:0, energyConsumedWh:0, tempStatus:'—', lifespanStatus:'—' };

const toHours   = (val, unit) => unit==='min' ? val/60  : unit==='s' ? val/3600 : val;
const toSeconds = (val, unit) => unit==='min' ? val*60  : unit==='h' ? val*3600 : val;

const usageTimeConv = min => {
    const m = Math.floor(min||0), s = Math.floor(((min||0)-m)*60);
    return `${m}m ${s}s`;
};

const lampStyle = (isOn, brightness, color) => {
    if (!isOn) return { background:'#f0f0f0', boxShadow:'none', border:'2px solid #ddd' };
    const a = 0.3+(brightness/100)*0.7;
    return color==='yellow'
        ? { background:`rgba(255,210,50,${a})`,  boxShadow:`0 0 ${20+brightness*0.4}px rgba(255,200,0,${a})`,   border:'2px solid rgba(255,180,0,0.6)' }
        : { background:`rgba(80,160,255,${a})`,  boxShadow:`0 0 ${20+brightness*0.4}px rgba(80,160,255,${a})`,  border:'2px solid rgba(80,160,255,0.6)' };
};

const badge = val => {
    if (!val||val==='—') return <span className="model-badge">—</span>;
    const c = (val==='CRITICAL'||val==='REPLACE_SOON') ? 'red'
            : (val==='WARNING'||val==='AGING'||val==='WARM') ? 'orange' : 'green';
    return <span className={`model-badge ${c}`}>{val}</span>;
};

//config en cache des lamps instances existantes en docker compose
const DEFAULT_LAMPS = [
    { lampId:'LAMP-001', room:'chambre2',    description:'Office lamp',    hasTemperatureSensor:true,  hasRoomTempSensor:true  },
    { lampId:'LAMP-002', room:'chambre', description:'House lamp', hasTemperatureSensor:false, hasRoomTempSensor:false },
];

export default function App() {
    const [phys, setPhys]             = useState({ isOn:false, brightness:0, temperature:22, usageMinutes:0, roomTemp:22 });
    const [twin, setTwin]             = useState(emptyModel);
    const [history, setHistory]       = useState([]);
    const [simHistory, setSimHistory] = useState([]);
    const [sims, setSims]             = useState([]);
    const [synced, setSynced]         = useState(false);
    const [lamps, setLamps]           = useState(DEFAULT_LAMPS);
    const [activeLampId, setActiveLampId] = useState('LAMP-001');
    const [twinLampId, setTwinLampId]     = useState('LAMP-001');
    const [showRegistry, setShowRegistry] = useState(false);
    const [newLamp, setNewLamp]       = useState({ lampId:'', description:'', hasTemperatureSensor:true, hasRoomTempSensor:true });
    const [roomTemp, setRoomTemp]           = useState(22);
    const [roomTempInput, setRoomTempInput] = useState(22);
    const [page, setPage]             = useState(1);
    const [simPage, setSimPage]       = useState(1);
    const [selectedSim, setSelectedSim] = useState(null);
    const [simVal, setSimVal]         = useState(60);
    const [simUnit, setSimUnit]       = useState('s');
    const [simForm, setSimForm]       = useState({ description:'', stepIntervalMs:500 });
    const [steps, setSteps]           = useState([{ brightness:50, roomTemp:22, val:100, unit:'h' }]);

    const PER_PAGE   = 7;
    const dragging   = React.useRef(false);
    const simFormRef = React.useRef(null);
    const activeLamp = lamps.find(l => l.lampId === activeLampId) || lamps[0];

    const getPhysUrl = (lampId) => P(lampIdToPort(lampId));
    const getEnvUrl  = (lamp)   => E(lamp?.room ?? 'default');

    // get physical state
    const getPhys = async () => {
        if (dragging.current) return;
        try {
            const r = await axios.get(`${getPhysUrl(activeLampId)}/state`);
            const d = r.data;
            setPhys({ isOn:d.isOn, brightness:d.brightness, temperature:d.temperature,
                      usageMinutes:d.usageMinutes, roomTemp:d.roomTemp });
        } catch(e) {}
    };

    // get env
    const getEnv = async () => {
        const lamp = lamps.find(l => l.lampId === activeLampId) || lamps[0];
        const r = await axios.get(`${getEnvUrl(lamp)}/state`).catch(() => ({ data:null }));
        if (r.data) { setRoomTemp(r.data.roomTemp); setRoomTempInput(r.data.roomTemp); }
    };

    //get twin part
    const getAll = async () => {
        try {
            const [t, ph, sh, sm, sy, ...regs] = await Promise.allSettled([
                axios.get(`${T}/state`),
                axios.get(`${S}/physical/last/50`),
                axios.get(`${S}/simulations`),
                axios.get(`${T}/simulations`),
                axios.get(`${T}/sync`),
                ...DEFAULT_LAMPS.map(l => axios.get(R(lampIdToPort(l.lampId)))),
            ]);
            if (t.status==='fulfilled'  && t.value.data)  setTwin(t.value.data);
            if (ph.status==='fulfilled')                   setHistory([...ph.value.data].reverse());
            if (sh.status==='fulfilled')                   setSimHistory(sh.value.data || []);
            if (sm.status==='fulfilled')                   setSims(sm.value.data || []);
            if (sy.status==='fulfilled') {
                setSynced(sy.value.data.twinned ?? false);
                if (sy.value.data.twinedLampId) setTwinLampId(sy.value.data.twinedLampId);
            }
            const configs = regs.filter(r => r.status==='fulfilled' && r.value.data).map(r => r.value.data);
            if (configs.length > 0) {
                setLamps(prev => prev.map(existing => {
                    const fresh = configs.find(c => c.lampId === existing.lampId);
                    return fresh ? { ...existing, ...fresh } : existing;
                }));
            }
        } catch(e) {}
    };

    useEffect(() => {
        DEFAULT_LAMPS.forEach(l => axios.get(R(lampIdToPort(l.lampId))).catch(() => {}));
        const id = setInterval(getAll, 1000);
        getAll();
        return () => clearInterval(id);
    }, []);

    useEffect(() => {
        const id = setInterval(() => {
            getPhys();
            getEnv();
        }, 1000);
        return () => clearInterval(id);
    }, [activeLampId]);

    useEffect(() => {
        setPhys({ isOn:false, brightness:0, temperature:22, usageMinutes:0, roomTemp:22 });
        getPhys();
        getEnv();
    }, [activeLampId]);

    const turnOn  = () => axios.post(`${getPhysUrl(activeLampId)}/on?brightness=${phys.brightness||100}`).then(getPhys);
    const turnOff = () => axios.post(`${getPhysUrl(activeLampId)}/off`).then(getPhys);
    const twinOn  = () => axios.post(`${T}/command`, { action:"ON"  }).then(getAll);
    const twinOff = () => axios.post(`${T}/command`, { action:"OFF" }).then(getAll);

    const setBright = v => {
        dragging.current = true;
        setPhys(p => ({ ...p, brightness:+v }));
        axios.post(`${getPhysUrl(activeLampId)}/brightness`, { brightness:+v })
             .finally(() => { dragging.current = false; });
    };

    const setEnvTemp = v => {
        setRoomTempInput(+v);
        const lamp = lamps.find(l => l.lampId === activeLampId) || lamps[0];
        axios.post(`${getEnvUrl(lamp)}/temp`, { temperature:+v });
    };

    const toggleSync = async () => {
        const next = !synced;
        if (next) {
            await Promise.all([
                axios.post(`${T}/sync/enable`, { lampId: twinLampId }),
                axios.post(`${S}/sync/enable`, null, { params: { lampId: twinLampId } }),
            ]);
        } else {
            await Promise.all([
                axios.post(`${T}/sync/disable`),
                axios.post(`${S}/sync/disable`),
            ]);
        }
        setSynced(next);
    };

    const registerLamp = async () => {
        const enriched = { ...newLamp, host:lampIdToHost(newLamp.lampId), port:lampIdToPort(newLamp.lampId) };
        await axios.post(R(enriched.port), enriched);
        setLamps(l => [...l.filter(x => x.lampId !== enriched.lampId), enriched]);
        setShowRegistry(false);
    };

    //launch sim
    const simConfigCache = React.useRef({});
    const launchSim = () => {
        const simDurationSeconds = toSeconds(simVal, simUnit);
        const mappedSteps = steps.map(s => ({
            brightness: s.brightness,
            roomTemp: s.roomTemp,
            realDurationHours: toHours(s.val, s.unit)
        }));
        axios.post(`${T}/simulations`, { ...simForm, simDurationSeconds, steps: mappedSteps })
            .then(res => {
                const simId = res.data?.simId;
                if (simId) {
                    simConfigCache.current[simId] = {
                        description:       simForm.description,
                        stepIntervalMs:    simForm.stepIntervalMs,
                        simDurationSeconds,
                        steps: steps.map(s => ({ ...s })),
                    };
                }
            });
    };
    const stopSim    = id => axios.delete(`${T}/simulations/${id}`);
    const pauseSim   = id => axios.post(`${T}/simulations/${id}/pause`);
    const resumeSim  = id => axios.post(`${T}/simulations/${id}/resume`);
    const stopAllSim = ()  => axios.delete(`${T}/simulations`);
    const addStep    = ()  => setSteps(p => [...p, { brightness:50, roomTemp:22, val:100, unit:'h' }]);
    const removeStep = i   => setSteps(p => p.filter((_,idx) => idx !== i));
    const updateStep = (i,f,v) => setSteps(p => p.map((s,idx) => idx===i ? { ...s,[f]:v } : s));


    const loadSimIntoForm = (simId) => {
        const cached = simConfigCache.current[simId];
        if (!cached) {
            console.warn('No cached config for', simId);
            return;
        }
        setSimForm({
            description:    cached.description    || '',
            stepIntervalMs: cached.stepIntervalMs || 500,
        });
        setSimVal(cached.simDurationSeconds || 60);
        setSimUnit('s');
        setSteps(cached.steps.map(s => ({ ...s }))); // already {brightness, roomTemp, val, unit}
        simFormRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const totalPages      = Math.ceil(history.length / PER_PAGE);
    const pageRecords     = history.slice((page-1)*PER_PAGE, page*PER_PAGE);
    const simIds          = [...new Set(simHistory.map(r => r.lampId))].sort();
    const simTotalPages   = Math.ceil(simIds.length / PER_PAGE);
    const simPageIds      = simIds.slice((simPage-1)*PER_PAGE, simPage*PER_PAGE);
    const selectedRecords = selectedSim ? simHistory.filter(r => r.lampId === selectedSim) : [];

    return (
        <div className="app">
            <header className="app-header"><h1>Lamp Digital Twin</h1></header>
            <div className="top-panels">

                {/* physical */}
                <div className="card">
                    <h2>Physical Lamp</h2>
                    <div className="lamp-selector">
                        <label>Physical lamp:</label>
                        <select value={activeLampId} onChange={e => setActiveLampId(e.target.value)}>
                            {lamps.map(l => <option key={l.lampId} value={l.lampId}>{l.lampId} _ {l.description}</option>)}
                        </select>
                    </div>



                    <div className="lamp-circle" style={lampStyle(phys.isOn, phys.brightness, 'yellow')}>
                        <span className="lamp-temp">{phys.temperature < 0 ? '—' : `${phys.temperature?.toFixed(1)}°C`}</span>
                    </div>
                    <div className="info-row"><span>Status</span>      <span className={phys.isOn?'badge on':'badge off'}>{phys.isOn?'ON':'OFF'}</span></div>
                    <div className="info-row"><span>Brightness</span>  <span>{phys.brightness?.toFixed(0)}%</span></div>
                    <div className="info-row"><span>Temperature</span> <span>{phys.temperature < 0 ? '—' : `${phys.temperature?.toFixed(1)}°C`}</span></div>
                    <div className="info-row"><span>Usage</span>       <span>{usageTimeConv(phys.usageMinutes)}</span></div>

                    <div className="btn-row">
                        <button className="btn green" onClick={turnOn}>ON</button>
                        <button className="btn red"   onClick={turnOff}>OFF</button>
                    </div>
                    <div className="slider-wrap">
                        <label>Brightness: <strong>{phys.brightness?.toFixed(0)}%</strong></label>
                        <input type="range" min="0" max="100" value={phys.brightness}
                               onChange={e => setBright(e.target.value)} />
                    </div>

                    <div className="env-section">
                        <div className="env-title">Environment</div>
                        <div className="info-row"><span>Room Temp</span><span>{roomTemp?.toFixed(1)}°C</span></div>
                        {activeLamp?.hasRoomTempSensor ? (
                            <>
                                <div className="slider-wrap">
                                    <input type="range" min="10" max="45" value={roomTempInput}
                                           onChange={e => setEnvTemp(e.target.value)} />
                                </div>
                                <p className="env-note">Sets room temp for <strong>{activeLamp?.room ?? 'default'}</strong>It affects all lamps associated in this room</p>
                            </>
                        ) : (
                            <p className="env-note">No temp sensor for <strong>{activeLamp?.room ?? 'default'}</strong>. It uses the old last room Temp value</p>
                        )}
                    </div>


                </div>

                {/* twin */}
                <div className="card">
                    <h2>Virtual Lamp {synced && <span className="sync-badge">Twinned</span>}</h2>
                    <div className="lamp-selector">
                        <label>Twin with:</label>
                        <select value={twinLampId} onChange={e => setTwinLampId(e.target.value)} disabled={synced}>
                            {lamps.map(l => <option key={l.lampId} value={l.lampId}>{l.lampId} _ {l.description}</option>)}
                        </select>
                    </div>
                    <div className="lamp-circle" style={lampStyle(twin.isOn, twin.brightness, 'blue')}>
                        <span className="lamp-temp">{twin.temperature?.toFixed(1)}°C</span>
                    </div>
                    <div className="info-row"><span>Status</span>      <span className={twin.isOn?'badge on':'badge off'}>{twin.isOn?'ON':'OFF'}</span></div>
                    <div className="info-row"><span>Brightness</span>  <span>{twin.brightness?.toFixed(0)}%</span></div>
                    <div className="info-row"><span>Temperature</span> <span>{twin.temperature?.toFixed(1)}°C</span></div>
                    <div className="info-row"><span>Usage</span>       <span>{usageTimeConv(twin.usageMinutes)}</span></div>
                    <div className="info-row"><span>Room Temp</span>   <span>{twin.roomTemp?.toFixed(1)}°C</span></div>
                    <div className="model-section">
                        <div className="info-row"><span>Power</span>     <span>{twin.powerWatts?.toFixed(1)} W</span></div>
                        <div className="info-row"><span>Energy</span>    <span>{twin.energyConsumedWh?.toFixed(4)} Wh</span></div>
                        <div className="info-row"><span>Thermal</span>   {badge(twin.tempStatus)}</div>
                        <div className="info-row"><span>Lifespan</span>  {badge(twin.lifespanStatus)}</div>
                        <div className="info-row"><span>Remaining</span> <span>{twin.remainingLifespanMinutes?.toFixed(0)} min</span></div>
                    </div>
                    <div className="twin-controls">
                        <button className={`btn ${synced?'red':'green'} full-width`} onClick={toggleSync}>
                            {synced ? 'Untwin' : `Twin with ${twinLampId}`}
                        </button>
                        <div className="btn-row twin-cmd-row">
                            <button className="btn green" onClick={twinOn}  disabled={!synced}>Turn ON</button>
                            <button className="btn red"   onClick={twinOff} disabled={!synced}>Turn OFF</button>
                        </div>
                    </div>
                </div>

                {/* sim */}
                <div className="card sim-card">
                    <h2>Simulations</h2>
                    <div className="sim-form" ref={simFormRef}>
                        <input type="text" placeholder="title" value={simForm.description}
                               onChange={e => setSimForm({ ...simForm, description:e.target.value })} />
                        <div className="steps-header">
                            <span className="time-row-label" style={{ margin:0 }}>Define simulation scenario</span>
                            <button className="btn-sm blue" onClick={addStep}>+ Add step</button>
                        </div>
                        {steps.map((s,i) => (
                            <div key={i} className="step-row">
                                <div className="step-number">{i+1}</div>
                                <div className="step-fields">
                                    <div className="step-field">
                                        <label>Brightness <strong>{s.brightness}%</strong></label>
                                        <input type="range" min="0" max="100" value={s.brightness}
                                               onChange={e => updateStep(i,'brightness',+e.target.value)} />
                                    </div>
                                    <div className="step-field">
                                        <label>Room temp <strong>{s.roomTemp}°C</strong></label>
                                        <input type="range" min="10" max="45" value={s.roomTemp}
                                               onChange={e => updateStep(i,'roomTemp',+e.target.value)} />
                                    </div>
                                    <div className="step-field">
                                        <label>Real duration</label>
                                        <div className="time-input-row" style={{ marginBottom:0 }}>
                                            <input type="number" min="1" value={s.val}
                                                   onChange={e => updateStep(i,'val',+e.target.value)} />
                                            <select value={s.unit} onChange={e => updateStep(i,'unit',e.target.value)}>
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
                        <div className="time-row-label">Simulation duration</div>
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
                                <label>Update interval (ms)</label>
                                <input type="number" min="100" value={simForm.stepIntervalMs}
                                       onChange={e => setSimForm({ ...simForm, stepIntervalMs:+e.target.value })} />
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
                                        <span className="timescale-badge">×{Math.round((s.totalRealDurationHours*3600)/s.simDurationSeconds).toLocaleString()}</span>
                                    </div>
                                    <p className="sim-desc">{s.description}</p>

                                    {s.steps && s.steps.length > 1 && (
                                        <div className="step-indicators">
                                            {s.steps.map((st,i) => {
                                                const startH = s.steps.slice(0,i).reduce((a,x) => a+x.realDurationHours, 0);
                                                const active = (s.realHoursElapsed||0) >= startH
                                                            && (s.realHoursElapsed||0) <  startH + st.realDurationHours;
                                                return <div key={i} className={`step-pill ${active?'active':''}`}
                                                            style={{ flex:st.realDurationHours }}><span>{i+1}</span></div>;
                                            })}
                                        </div>
                                    )}
                                    <div className="progress-bar">
                                        <div className="progress-fill" style={{ width:`${s.progressPercent||0}%` }} />
                                    </div>
                                    <span className="progress-label">{s.progressPercent||0}%</span>
                                    {m && (
                                        <div className="sim-model-row">
                                            <span>{m.powerWatts?.toFixed(1)}W</span>
                                            <span>{m.temperature?.toFixed(1)}°C</span>
                                            {badge(m.tempStatus)}{badge(m.lifespanStatus)}
                                        </div>
                                    )}
                                    <div className="sim-btns">
                                        {s.state==='RUNNING' && <button className="btn-sm blue" onClick={() => pauseSim(s.simId)}>Pause</button>}
                                        {s.state==='PAUSED'  && <button className="btn-sm blue" onClick={() => resumeSim(s.simId)}>Resume</button>}
                                        <button className="btn-sm red" onClick={() => stopSim(s.simId)}>Stop</button>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* shadows */}
            <div className="bottom-section">
                <div className="shadow-section">
                    <h2>Synchro Logs</h2>
                    <table className="shadow-table">
                        <thead><tr><th>Source</th><th>Status</th><th>Brightness</th><th>Temp</th><th>Power</th><th>Energy (Wh)</th><th>Thermal</th><th>Lifespan</th></tr></thead>
                        <tbody>{pageRecords.map((r,i) => (
                            <tr key={i}>
                                <td className="source-cell">{r.source}</td>
                                <td><span className={r.isOn?'badge on':'badge off'}>{r.isOn?'ON':'OFF'}</span></td>
                                <td>{r.brightness?.toFixed(0)}%</td>
                                <td>{r.temperature?.toFixed(1)}°C</td>
                                <td>{r.powerWatts?.toFixed(1)}W</td>
                                <td>{r.energyConsumedWh?.toFixed(4)}</td>
                                <td>{badge(r.tempStatus)}</td>
                                <td>{badge(r.lifespanStatus)}</td>
                            </tr>
                        ))}</tbody>
                    </table>
                    <div className="pagination">
                        <button className="btn-page" disabled={page===1} onClick={() => setPage(p => p-1)}>Prev</button>
                        <span>Page {page} of {totalPages||1}</span>
                        <button className="btn-page" disabled={page>=totalPages} onClick={() => setPage(p => p+1)}>Next</button>
                    </div>
                </div>

                <div className="sim-history-section">
                    <h2>Simulation Logs</h2>
                    {simIds.length === 0 ? <p className="empty-note">No simulation records yet</p> : <>
                        <table className="shadow-table">
                            <thead><tr><th>Sim ID</th><th>Records</th><th>Last Brightness</th><th>Last Temp</th><th>Last Power</th><th>Lifespan</th><th></th></tr></thead>
                            <tbody>{simPageIds.map(id => {
                                const records = simHistory.filter(r => r.lampId === id);
                                const last    = records[records.length-1];
                                return (
                                    <tr key={id} className="sim-history-row">
                                        <td><button className="sim-id-btn" onClick={() => setSelectedSim(id)}>{id}</button></td>
                                        <td>{records.length}</td>
                                        <td>{last?.brightness?.toFixed(0)}%</td>
                                        <td>{last?.temperature?.toFixed(1)}°C</td>
                                        <td>{last?.powerWatts?.toFixed(1)}W</td>
                                        <td>{badge(last?.lifespanStatus)}</td>
                                        <td style={{ display:'flex', gap:'4px' }}>
                                            <button className="btn-sm green" onClick={() => loadSimIntoForm(id)}> Restart</button>
                                        </td>
                                    </tr>
                                );
                            })}</tbody>
                        </table>
                        <div className="pagination">
                            <button className="btn-page" disabled={simPage===1} onClick={() => setSimPage(p => p-1)}>Prev</button>
                            <span>Page {simPage} of {simTotalPages||1}</span>
                            <button className="btn-page" disabled={simPage>=simTotalPages} onClick={() => setSimPage(p => p+1)}>Next</button>
                        </div>
                    </>}
                </div>
            </div>

            {selectedSim && (
                <div className="popup-overlay" onClick={() => setSelectedSim(null)}>
                    <div className="popup" onClick={e => e.stopPropagation()}>
                        <div className="popup-header">
                            <h3>{selectedSim} — Results</h3>
                            <button className="popup-close" onClick={() => setSelectedSim(null)}>✕</button>
                        </div>
                        <div className="popup-body">
                            <table className="shadow-table">
                                <thead><tr><th>Status</th><th>Brightness</th><th>Temp</th><th>Power</th><th>Energy (Wh)</th><th>Usage (min)</th><th>Remaining (min)</th><th>Thermal</th><th>Lifespan</th></tr></thead>
                                <tbody>{selectedRecords.map((r,i) => (
                                    <tr key={r.tupleId ?? i}>
                                        <td><span className={r.isOn?'badge on':'badge off'}>{r.isOn?'ON':'OFF'}</span></td>
                                        <td>{r.brightness?.toFixed(0)}%</td>
                                        <td>{r.temperature?.toFixed(1)}°C</td>
                                        <td>{r.powerWatts?.toFixed(1)}W</td>
                                        <td>{r.energyConsumedWh?.toFixed(4)}</td>
                                        <td>{r.usageMinutes?.toFixed(2)}</td>
                                        <td>{r.remainingLifespanMinutes?.toFixed(0)}</td>
                                        <td>{badge(r.tempStatus)}</td>
                                        <td>{badge(r.lifespanStatus)}</td>
                                    </tr>
                                ))}</tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

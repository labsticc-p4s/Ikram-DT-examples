import axios from 'axios';

const BASE = process.env.REACT_APP_GATEWAY_URL || 'http://localhost:8086';

export const GW = BASE;
export const P  = `${BASE}/api/physical`;
export const T  = `${BASE}/api/twin`;
export const S  = `${BASE}/api/shadow`;
export const ST = `${BASE}/api/strain`;
export const M  = `${BASE}/api/models`;

export const PER_PAGE = 7;

export const fmt2   = v => (v ?? 0).toFixed(2);
export const fmt4   = v => (v ?? 0).toFixed(4);
export const fmtPop = v => {
    if (!v) return '0';
    if (v >= 1e9) return (v / 1e9).toFixed(2) + ' ×10⁹';
    if (v >= 1e6) return (v / 1e6).toFixed(2) + ' ×10⁶';
    return Math.round(v).toLocaleString();
};

export const statusClass = s =>
    s === 'OPTIMAL' ? 'optimal' : s === 'SUBOPTIMAL' ? 'suboptimal' : s ? 'inhibited' : '';

export const badge = val => {
    if (!val || val === '—') return <span className="model-badge">—</span>;
    const c = val === 'OPTIMAL' ? 'green' : val === 'SUBOPTIMAL' ? 'orange' : 'red';
    return <span className={`model-badge ${c}`}>{val}</span>;
};

export const toHours   = (val, unit) => unit === 'min' ? val / 60  : unit === 's' ? val / 3600 : val;
export const toSeconds = (val, unit) => unit === 'min' ? val * 60  : unit === 'h' ? val * 3600 : val;

export const defaultSimStrain = () => ({
    strainId:       `SIM-STRAIN-${Date.now()}`,
    name:           'Sim Strain',
    muMax:          0.8,
    populationInit: 1_000_000,
    populationMax:  1e10,
    latency:        0.01,
    phMin: 5.0, phOpt: 7.0, phMax: 9.0,
    tempMin: 25.0, tempOpt: 37.0, tempMax: 45.0,
    open: false,
});

export const invalidateCache = (strainId) =>
    axios.post(`${BASE}/api/gateway/cache/invalidate/${strainId}`).catch(() => {});

export const fmtTime = (minutes) => {
    if (!minutes && minutes !== 0) return "0 min";

    const h = Math.floor(minutes / 60);
    const min = Math.round(minutes % 60);

    if (h === 0) {
        return `${min} min`;
    }

    return `${h} h ${min} min`;
};
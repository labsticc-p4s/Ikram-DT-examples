import React from 'react';

const BASE = process.env.REACT_APP_GATEWAY_URL || 'http://localhost:8086';

export const PH  = `${BASE}/api/physical`;
export const ST  = `${BASE}/api/strain`;
export const GW  = `${BASE}/api/twin`;
export const SIM = `${BASE}/api/simulation`;
export const EXP = `${BASE}/api/experiments`;

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

export const PH_MODELS   = ['cardinal', 'quadratic'];
export const TEMP_MODELS = ['cardinal', 'quadratic'];
export const POP_MODELS  = ['logistic', 'gompertz'];

export const defaultSimStep = () => ({ ph: 7.0, temperature: 37.0, realDurationMin: 60 });
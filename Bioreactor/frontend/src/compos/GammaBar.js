import React from 'react';
import { fmt2 } from '../helpers/constants';

export default function GammaBar({ label, value }) {
    const pct = Math.max(0, Math.min(100, Math.round((value ?? 0) * 100)));
    const cls = pct >= 80 ? '' : pct >= 40 ? 'suboptimal' : 'inhibited';
    return (
        <div className="gamma-bar-wrap">
            <label><span>{label}</span><span>{fmt2(value)}</span></label>
            <div className="gamma-bar">
                <div className={`gamma-fill ${cls}`} style={{ width: `${pct}%` }} />
            </div>
        </div>
    );
}
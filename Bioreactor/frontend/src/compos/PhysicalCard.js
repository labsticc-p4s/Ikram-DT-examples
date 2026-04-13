import React from 'react';
import { fmt2, fmtTime  } from '../helpers/constants';
import StrainManagementPanel from './StrainManagementPanel';

export default function PhysicalCard({ phys, physStrains, onStrainsChanged }) {
    return (
        <div className="card">
            <h2>Physical Reactor</h2>
            <div className="reactor-circle">
                <span className="reactor-label">{phys.reactorId}</span>
            </div>
            <div className="info-row"><span>pH</span>          <span>{fmt2(phys.ph)}</span></div>
            <div className="info-row"><span>Temperature</span> <span>{fmt2(phys.temperature)} °C</span></div>
            <div className="info-row"><span>Time passed</span> <span>{fmtTime(phys.hours)}</span></div>

            <StrainManagementPanel physStrains={physStrains} onStrainsChanged={onStrainsChanged} />
        </div>
    );
}
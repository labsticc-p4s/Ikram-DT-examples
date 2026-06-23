import React from 'react';
import { fmt2 } from '../helpers/constants';
import StrainManagementPanel from './StrainManagementPanel';

export default function PhysicalCard({
    phys, registered, registering, register, flash,
    families, initials,
    famDraft, setFamDraft,
    initDraft, setInitDraft,
    saving, strainFlash,
    saveFamily, saveInitial,
}) {
    const readings = phys?.sensorReadings ?? {};
    const sensors  = Object.entries(readings);

    return (
        <div className="card">
            <h2>Physical Bioreactor</h2>

            <div className="reactor-circle">
                <span className="reactor-label">{phys?.reactorId || '—'}</span>
            </div>

            {sensors.length === 0
                ? <p className="empty-note">No sensor data yet</p>
                : sensors.map(([key, val]) => (
                    <div className="info-row" key={key}>
                        <span>{key}</span>
                        <span>{fmt2(val)}</span>
                    </div>
                ))
            }

            <div className="env-section">
                <div className="env-title">Registration</div>
                {flash && (
                    <div className={`strain-flash ${flash.ok ? 'ok' : 'err'}`}>{flash.msg}</div>
                )}
                {registered
                    ? <div className="strain-flash ok">Bioreactor registered ✓</div>
                    : (
                        <button
                            className="btn blue full-width"
                            onClick={register}
                            disabled={registering}
                        >
                            {registering ? 'Registering…' : 'Register Bioreactor'}
                        </button>
                    )
                }
            </div>

            <StrainManagementPanel
                families={families}
                initials={initials}
                famDraft={famDraft}
                setFamDraft={setFamDraft}
                initDraft={initDraft}
                setInitDraft={setInitDraft}
                saving={saving}
                flash={strainFlash}
                saveFamily={saveFamily}
                saveInitial={saveInitial}
            />
        </div>
    );
}
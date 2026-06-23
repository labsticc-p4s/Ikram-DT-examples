import React, { useState } from 'react';
import { FamilyEditor, FamilySummary, InitialStrainEditor } from './StrainEditor';

export default function StrainManagementPanel({
    families, initials,
    famDraft, setFamDraft,
    initDraft, setInitDraft,
    saving, flash,
    saveFamily, saveInitial,
}) {
    const [tab, setTab] = useState('family');

    return (
        <div className="card">
            <h2>Strain Management</h2>

            <div className="tab-bar">
                <button
                    className={`tab-btn${tab === 'family' ? ' active' : ''}`}
                    onClick={() => setTab('family')}
                >Strain Families</button>
                <button
                    className={`tab-btn${tab === 'initial' ? ' active' : ''}`}
                    onClick={() => setTab('initial')}
                >Initial Conditions</button>
            </div>

            {flash && (
                <div className={`strain-flash ${flash.ok ? 'ok' : 'err'}`}>{flash.msg}</div>
            )}

            {tab === 'family' && (
                <>
                    <div className="env-section">
                        <div className="env-title">Registered Families</div>
                        {families.length === 0
                            ? <p className="empty-note">No families yet.</p>
                            : families.map(f => (
                                <div
                                    key={f.strainId}
                                    className="strain-list-item"
                                    onClick={() => setFamDraft({ ...f })}
                                >
                                    <strong>{f.name || f.strainId}</strong>
                                    <span className="strain-id-tag">{f.strainId}</span>
                                    <FamilySummary family={f} />
                                </div>
                            ))
                        }
                    </div>

                    <div className="env-section">
                        <div className="env-title">
                            {families.find(f => f.strainId === famDraft.strainId) ? 'Edit Family' : 'New Family'}
                        </div>
                        <FamilyEditor family={famDraft} onChange={setFamDraft} />
                        <button
                            className="btn green full-width"
                            onClick={saveFamily}
                            disabled={saving}
                        >
                            {saving ? 'Saving…' : 'Save Family'}
                        </button>
                    </div>
                </>
            )}

            {tab === 'initial' && (
                <>
                    <div className="env-section">
                        <div className="env-title">Registered Conditions</div>
                        {initials.length === 0
                            ? <p className="empty-note">No conditions yet.</p>
                            : initials.map(c => (
                                <div
                                    key={c.condId}
                                    className="strain-list-item"
                                    onClick={() => setInitDraft({ ...c })}
                                >
                                    <strong>{c.condId}</strong>
                                    <div style={{ fontSize: 11, color: '#888', marginTop: 2 }}>
                                        N₀={c.populationInit.toExponential(1)} · Nmax={c.populationMax.toExponential(1)} · {c.familyIds.length} famil{c.familyIds.length === 1 ? 'y' : 'ies'}
                                    </div>
                                </div>
                            ))
                        }
                    </div>

                    <div className="env-section">
                        <div className="env-title">
                            {initials.find(c => c.condId === initDraft.condId) ? 'Edit Condition' : 'New Condition'}
                        </div>
                        <InitialStrainEditor
                            strain={initDraft}
                            families={families}
                            onChange={setInitDraft}
                        />
                        <button
                            className="btn green full-width"
                            onClick={saveInitial}
                            disabled={saving}
                        >
                            {saving ? 'Saving…' : 'Save Condition'}
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}
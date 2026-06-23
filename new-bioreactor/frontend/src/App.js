import React from 'react';
import './App.css';

import { useReactor }       from './hooks/useReactor';
import { useStrains }       from './hooks/useStrains';
import { useExperiment }    from './hooks/useExperiment';
import { useSimulations }   from './hooks/useSimulations';
import { useStoragePanel }  from './hooks/useStoragePanel';

import PhysicalCard          from './compos/PhysicalCard';
import TwinCard              from './compos/TwinCard';
import SimulationsCard       from './compos/SimulationsCard';
import StoragePanel          from './compos/StoragePanel';

export default function App() {
    const reactor  = useReactor();
    const strains  = useStrains();
    const exp      = useExperiment();
    const sim      = useSimulations();
    const storage  = useStoragePanel();

    // Physical twin start — registers entry in storage, does NOT touch sim state
    const handleStartTwin = () => exp.startTwin(entry => {
        if (entry) storage.registerExperiment(entry);
    });

    // Sim launch — registers entry in storage, does NOT touch physical twin state
    const handleLaunchSim = () => sim.launchSim(entry => {
        if (entry) storage.registerExperiment(entry);
    });

    return (
        <div className="app">
            <header className="app-header">
                <h1>Bioreactor Digital Twin</h1>
            </header>

            <div className="top-panels">
                <PhysicalCard
                    phys={reactor.phys}
                    registered={reactor.registered}
                    registering={reactor.registering}
                    register={reactor.register}
                    flash={reactor.flash}
                    families={strains.families}
                    initials={strains.initials}
                    famDraft={strains.famDraft}
                    setFamDraft={strains.setFamDraft}
                    initDraft={strains.initDraft}
                    setInitDraft={strains.setInitDraft}
                    saving={strains.saving}
                    strainFlash={strains.flash}
                    saveFamily={strains.saveFamily}
                    saveInitial={strains.saveInitial}
                />

                {/* TwinCard only ever receives physical twin state from useExperiment */}
                <TwinCard
                    form={exp.form}
                    setForm={exp.setForm}
                    twinActive={exp.twinActive}
                    loading={exp.loading}
                    flash={exp.flash}
                    activeExpId={exp.activeExpId}
                    startTwin={handleStartTwin}
                    stopTwin={exp.stopTwin}
                    initials={strains.initials}
                    twin={exp.twin}
                />

                {/* SimulationsCard: launches sims, no longer tracks a single "current" live state
                    since multiple simulations can run in parallel. */}
                <SimulationsCard
                    form={sim.form}
                    setForm={sim.setForm}
                    steps={sim.steps}
                    addStep={sim.addStep}
                    removeStep={sim.removeStep}
                    updateStep={sim.updateStep}
                    launching={sim.launching}
                    flash={sim.flash}
                    launchSim={handleLaunchSim}
                    lastSimId={sim.lastSimId}
                    initials={strains.initials}
                />
            </div>

            <div className="bottom-section">
                <StoragePanel
                    experiments={storage.experiments}
                    loading={storage.loading}
                    onRefresh={storage.refreshExperiment}
                    onRefreshAll={storage.refreshAll}
                />
            </div>
        </div>
    );
}
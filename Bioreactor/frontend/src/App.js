import React, { useState } from 'react';
import './App.css';
import { useReactor }     from './hooks/useReactor';
import { useSimulations } from './hooks/useSimulations';
import PhysicalCard       from './compos/PhysicalCard';
import TwinCard           from './compos/TwinCard';
import SimulationsCard    from './compos/SimulationsCard';
import ShadowCard     from './compos/ShadowCard';

export default function App() {
    const reactor = useReactor();
    const sims    = useSimulations();
    const [page,  setPage]  = useState(1);

    return (
        <div className="app">
            <header className="app-header"><h1>Bioreactor Digital Twin</h1></header>

            <div className="top-panels">
                <PhysicalCard
                    phys={reactor.phys}
                    physStrains={reactor.physStrains}
                    onStrainsChanged={reactor.fetchPhysStrains}
                />
                <TwinCard
                    twin={reactor.twin}
                    synced={reactor.synced}
                    toggleSync={reactor.toggleSync}
                    physStrains={reactor.physStrains}
                    selectedStrainIds={reactor.selectedStrainIds}
                    toggleStrainSelection={reactor.toggleStrainSelection}
                    availableModels={reactor.availableModels}
                    modelSelection={reactor.modelSelection}
                    setModelSelection={reactor.setModelSelection}
                    applyModelSelection={reactor.applyModelSelection}
                />
                <SimulationsCard
                    sims={sims.sims}
                    simForm={sims.simForm}        setSimForm={sims.setSimForm}
                    simVal={sims.simVal}          setSimVal={sims.setSimVal}
                    simUnit={sims.simUnit}        setSimUnit={sims.setSimUnit}
                    steps={sims.steps}
                    addStep={sims.addStep}        removeStep={sims.removeStep}
                    updateStep={sims.updateStep}  updateStepStrains={sims.updateStepStrains}
                    launchSim={sims.launchSim}    stopSim={sims.stopSim}
                    pauseSim={sims.pauseSim}      resumeSim={sims.resumeSim}
                    stopAllSim={sims.stopAllSim}
                />
            </div>

            <div className="bottom-section">
                <ShadowCard
                    history={reactor.history}
                    page={page} setPage={setPage}
                    simPageIds={sims.simPageIds}
                    simTotalPages={sims.simTotalPages}
                    simPage={sims.simPage}         setSimPage={sims.setSimPage}
                    simHistory={sims.simHistory}
                    selectedSim={sims.selectedSim} setSelectedSim={sims.setSelectedSim}
                    selectedRecs={sims.selectedRecs}
                />
            </div>
        </div>
    );
}
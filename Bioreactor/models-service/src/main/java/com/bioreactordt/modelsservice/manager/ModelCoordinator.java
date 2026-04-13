package com.bioreactordt.modelsservice.manager;

import com.bioreactordt.modelsservice.calculators.pH.*;
import com.bioreactordt.modelsservice.calculators.population.*;
import com.bioreactordt.modelsservice.calculators.temperature.*;
import com.bioreactordt.modelsservice.models.ModelSelection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ModelCoordinator {

    private final ConcurrentHashMap<String, ModelSelection> selections = new ConcurrentHashMap<>();

    private final Map<String, PhGammaFunction>    phModels  = Map.of(
            "cardinal", new CardinalPhGamma(),
            "quadratic",   new QuadraticPhGamma()
    );
    private final Map<String, TempGammaFunction>  tempModels = Map.of(
            "cardinal", new CardinalTempGamma(),
            "quadratic",   new QuadraticTempGamma()
    );
    private final Map<String, PopulationFunction> popModels  = Map.of(
            "logistic", new LogisticPopulation(),
            "gompertz",   new GompertzPopulation()
    );

    public void select(ModelSelection s) {
        selections.put(s.getReactorId(), s);
        log.info("Model selection updated ph={} temp={} pop={}",
                s.getPhModel(), s.getTemperatureModel(), s.getPopulationModel());
    }

    public void reset(String reactorId) {
        selections.remove(reactorId);
        popModels.values().forEach(m -> m.reset(reactorId));
    }

    public PhGammaFunction getPhModel(String reactorId) {
        String key = selections.getOrDefault(reactorId, defaultSelection(reactorId)).getPhModel();
        return phModels.getOrDefault(key, phModels.get("cardinal"));
    }

    public TempGammaFunction getTempModel(String reactorId) {
        String key = selections.getOrDefault(reactorId, defaultSelection(reactorId)).getTemperatureModel();
        return tempModels.getOrDefault(key, tempModels.get("cardinal"));
    }

    public PopulationFunction getPopModel(String reactorId) {
        String key = selections.getOrDefault(reactorId, defaultSelection(reactorId)).getPopulationModel();
        return popModels.getOrDefault(key, popModels.get("logistic"));
    }

    public ModelSelection getSelection(String reactorId) {
        return selections.getOrDefault(reactorId, defaultSelection(reactorId));
    }

    public List<String> availablePhModels()   { return List.copyOf(phModels.keySet()); }
    public List<String> availableTempModels() { return List.copyOf(tempModels.keySet()); }
    public List<String> availablePopModels()  { return List.copyOf(popModels.keySet()); }

    private ModelSelection defaultSelection(String reactorId) {
        return new ModelSelection(reactorId, "cardinal", "cardinal", "logistic");
    }


}
package org.example.vladtech.estimates.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.data.EstimateSettings;
import org.example.vladtech.estimates.data.EstimateSettingsRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateSettingsServiceImpl implements EstimateSettingsService {

    private final EstimateSettingsRepository estimateSettingsRepository;

    @Override
    public EstimateSettings getSettings() {
        return estimateSettingsRepository.findById(EstimateSettings.DEFAULT_ID)
                .map(this::applyDefaults)
                .orElseGet(() -> estimateSettingsRepository.save(EstimateSettings.defaultSettings()));
    }

    @Override
    public EstimateSettings updateSettings(EstimateSettings updates) {
        if (updates == null) {
            throw new IllegalArgumentException("Estimate settings payload is required");
        }

        EstimateSettings base = estimateSettingsRepository.findById(EstimateSettings.DEFAULT_ID)
                .map(this::applyDefaults)
                .orElseGet(EstimateSettings::defaultSettings);

        EstimateSettings merged = mergeSettings(base, updates);
        merged.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings saved = estimateSettingsRepository.save(merged);

        log.info("Estimate settings updated: id={}", saved.getId());
        return saved;
    }

    private EstimateSettings applyDefaults(EstimateSettings existing) {
        return mergeSettings(EstimateSettings.defaultSettings(), existing);
    }

    private EstimateSettings mergeSettings(EstimateSettings base, EstimateSettings updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings merged = new EstimateSettings();
        merged.setId(EstimateSettings.DEFAULT_ID);

        merged.setLaborRate(firstNonNull(updates.getLaborRate(), base.getLaborRate()));
        merged.setOverheadRate(firstNonNull(updates.getOverheadRate(), base.getOverheadRate()));
        merged.setContingencyRate(firstNonNull(updates.getContingencyRate(), base.getContingencyRate()));
        merged.setTaxRate(firstNonNull(updates.getTaxRate(), base.getTaxRate()));

        merged.setSidingExtraLaborPerStoryRate(firstNonNull(
                updates.getSidingExtraLaborPerStoryRate(), base.getSidingExtraLaborPerStoryRate()));
        merged.setRoofingExtraLaborPerStoryRate(firstNonNull(
                updates.getRoofingExtraLaborPerStoryRate(), base.getRoofingExtraLaborPerStoryRate()));
        merged.setInsulationAdderPerSqFt(firstNonNull(
                updates.getInsulationAdderPerSqFt(), base.getInsulationAdderPerSqFt()));
        merged.setRoofPitchFactorPerUnit(firstNonNull(
                updates.getRoofPitchFactorPerUnit(), base.getRoofPitchFactorPerUnit()));
        merged.setRoofTearOffCostPerSqFt(firstNonNull(
                updates.getRoofTearOffCostPerSqFt(), base.getRoofTearOffCostPerSqFt()));
        merged.setRoofSkylightCost(firstNonNull(
                updates.getRoofSkylightCost(), base.getRoofSkylightCost()));

        merged.setKitchenPlumbingCost(firstNonNull(
                updates.getKitchenPlumbingCost(), base.getKitchenPlumbingCost()));
        merged.setKitchenElectricalCost(firstNonNull(
                updates.getKitchenElectricalCost(), base.getKitchenElectricalCost()));

        merged.setWindowBaseCostPerUnit(firstNonNull(
                updates.getWindowBaseCostPerUnit(), base.getWindowBaseCostPerUnit()));
        merged.setDoorBaseCostPerUnit(firstNonNull(
                updates.getDoorBaseCostPerUnit(), base.getDoorBaseCostPerUnit()));
        merged.setWindowDoorLaborRateMultiplier(firstNonNull(
                updates.getWindowDoorLaborRateMultiplier(), base.getWindowDoorLaborRateMultiplier()));

        merged.setDeckBaseMaterialCostPerSqFt(firstNonNull(
                updates.getDeckBaseMaterialCostPerSqFt(), base.getDeckBaseMaterialCostPerSqFt()));
        merged.setDeckRailingCostPerLinearFoot(firstNonNull(
                updates.getDeckRailingCostPerLinearFoot(), base.getDeckRailingCostPerLinearFoot()));
        merged.setDeckStairsCost(firstNonNull(
                updates.getDeckStairsCost(), base.getDeckStairsCost()));
        merged.setDeckCoverCostPerSqFt(firstNonNull(
                updates.getDeckCoverCostPerSqFt(), base.getDeckCoverCostPerSqFt()));

        merged.setFloorSubfloorRepairCostPerSqFt(firstNonNull(
                updates.getFloorSubfloorRepairCostPerSqFt(), base.getFloorSubfloorRepairCostPerSqFt()));
        merged.setFloorRemovalBaseCostPerSqFt(firstNonNull(
                updates.getFloorRemovalBaseCostPerSqFt(), base.getFloorRemovalBaseCostPerSqFt()));

        merged.setSidingFactors(mergeSidingFactors(
                base.getSidingFactors(), updates.getSidingFactors()));
        merged.setRoofFactors(mergeRoofFactors(
                base.getRoofFactors(), updates.getRoofFactors()));
        merged.setWindowFactors(mergeWindowFactors(
                base.getWindowFactors(), updates.getWindowFactors()));
        merged.setDoorFactors(mergeDoorFactors(
                base.getDoorFactors(), updates.getDoorFactors()));
        merged.setDeckFactors(mergeDeckFactors(
                base.getDeckFactors(), updates.getDeckFactors()));
        merged.setFlooringFactors(mergeFlooringFactors(
                base.getFlooringFactors(), updates.getFlooringFactors()));
        merged.setFlooringRemovalFactors(mergeFlooringRemovalFactors(
                base.getFlooringRemovalFactors(), updates.getFlooringRemovalFactors()));

        return merged;
    }

    private EstimateSettings.SidingFactors mergeSidingFactors(
            EstimateSettings.SidingFactors base,
            EstimateSettings.SidingFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.SidingFactors merged = new EstimateSettings.SidingFactors();
        merged.setVinyl(firstNonNull(updates.getVinyl(), base.getVinyl()));
        merged.setWood(firstNonNull(updates.getWood(), base.getWood()));
        merged.setFiberCement(firstNonNull(updates.getFiberCement(), base.getFiberCement()));
        merged.setBrick(firstNonNull(updates.getBrick(), base.getBrick()));
        merged.setStoneVeneer(firstNonNull(updates.getStoneVeneer(), base.getStoneVeneer()));
        return merged;
    }

    private EstimateSettings.RoofFactors mergeRoofFactors(
            EstimateSettings.RoofFactors base,
            EstimateSettings.RoofFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.RoofFactors merged = new EstimateSettings.RoofFactors();
        merged.setAsphalt(firstNonNull(updates.getAsphalt(), base.getAsphalt()));
        merged.setMetal(firstNonNull(updates.getMetal(), base.getMetal()));
        merged.setClay(firstNonNull(updates.getClay(), base.getClay()));
        merged.setSlate(firstNonNull(updates.getSlate(), base.getSlate()));
        merged.setSynthetic(firstNonNull(updates.getSynthetic(), base.getSynthetic()));
        return merged;
    }

    private EstimateSettings.WindowFactors mergeWindowFactors(
            EstimateSettings.WindowFactors base,
            EstimateSettings.WindowFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.WindowFactors merged = new EstimateSettings.WindowFactors();
        merged.setCasement(firstNonNull(updates.getCasement(), base.getCasement()));
        merged.setSlider(firstNonNull(updates.getSlider(), base.getSlider()));
        merged.setDoubleHung(firstNonNull(updates.getDoubleHung(), base.getDoubleHung()));
        merged.setAwning(firstNonNull(updates.getAwning(), base.getAwning()));
        merged.setFixed(firstNonNull(updates.getFixed(), base.getFixed()));
        return merged;
    }

    private EstimateSettings.DoorFactors mergeDoorFactors(
            EstimateSettings.DoorFactors base,
            EstimateSettings.DoorFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.DoorFactors merged = new EstimateSettings.DoorFactors();
        merged.setWood(firstNonNull(updates.getWood(), base.getWood()));
        merged.setFiberglass(firstNonNull(updates.getFiberglass(), base.getFiberglass()));
        merged.setSteel(firstNonNull(updates.getSteel(), base.getSteel()));
        merged.setGlassPanel(firstNonNull(updates.getGlassPanel(), base.getGlassPanel()));
        return merged;
    }

    private EstimateSettings.DeckFactors mergeDeckFactors(
            EstimateSettings.DeckFactors base,
            EstimateSettings.DeckFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.DeckFactors merged = new EstimateSettings.DeckFactors();
        merged.setWood(firstNonNull(updates.getWood(), base.getWood()));
        merged.setComposite(firstNonNull(updates.getComposite(), base.getComposite()));
        merged.setPvc(firstNonNull(updates.getPvc(), base.getPvc()));
        merged.setAluminum(firstNonNull(updates.getAluminum(), base.getAluminum()));
        return merged;
    }

    private EstimateSettings.FlooringFactors mergeFlooringFactors(
            EstimateSettings.FlooringFactors base,
            EstimateSettings.FlooringFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.FlooringFactors merged = new EstimateSettings.FlooringFactors();
        merged.setHardwood(firstNonNull(updates.getHardwood(), base.getHardwood()));
        merged.setEngineeredHardwood(firstNonNull(updates.getEngineeredHardwood(), base.getEngineeredHardwood()));
        merged.setLaminate(firstNonNull(updates.getLaminate(), base.getLaminate()));
        merged.setVinyl(firstNonNull(updates.getVinyl(), base.getVinyl()));
        merged.setTile(firstNonNull(updates.getTile(), base.getTile()));
        merged.setCarpet(firstNonNull(updates.getCarpet(), base.getCarpet()));
        merged.setPolishedConcrete(firstNonNull(updates.getPolishedConcrete(), base.getPolishedConcrete()));
        return merged;
    }

    private EstimateSettings.FlooringRemovalFactors mergeFlooringRemovalFactors(
            EstimateSettings.FlooringRemovalFactors base,
            EstimateSettings.FlooringRemovalFactors updates) {
        if (updates == null) {
            return base;
        }

        EstimateSettings.FlooringRemovalFactors merged = new EstimateSettings.FlooringRemovalFactors();
        merged.setHardwood(firstNonNull(updates.getHardwood(), base.getHardwood()));
        merged.setEngineeredHardwood(firstNonNull(updates.getEngineeredHardwood(), base.getEngineeredHardwood()));
        merged.setLaminate(firstNonNull(updates.getLaminate(), base.getLaminate()));
        merged.setVinyl(firstNonNull(updates.getVinyl(), base.getVinyl()));
        merged.setTile(firstNonNull(updates.getTile(), base.getTile()));
        merged.setCarpet(firstNonNull(updates.getCarpet(), base.getCarpet()));
        merged.setPolishedConcrete(firstNonNull(updates.getPolishedConcrete(), base.getPolishedConcrete()));
        return merged;
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }
}

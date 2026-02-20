package org.example.vladtech.estimates.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.data.EstimateSettings;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.data.windowanddoor.WindowDoorReplace;
import org.example.vladtech.estimates.data.windowanddoor.WindowType;
import org.example.vladtech.estimates.data.windowanddoor.DoorType;
import org.example.vladtech.estimates.data.patio.DeckPatioAddition;
import org.example.vladtech.estimates.data.patio.DeckMaterial;
import org.example.vladtech.estimates.data.floor.FloorReplace;
import org.example.vladtech.estimates.data.shared.FlooringMaterial;
import org.example.vladtech.estimates.exceptions.EstimationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimationServiceImpl implements EstimationService {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final EstimateSettingsService estimateSettingsService;

    @Override
    public RenovationProject calculateEstimate(RenovationProject project) {
        if (project == null) {
            throw new EstimationException("E000", "Project cannot be null");
        }

        EstimateSettings settings = estimateSettingsService.getSettings();
        BigDecimal laborRate = settings.getLaborRate();
        BigDecimal overheadRate = settings.getOverheadRate();
        BigDecimal contingencyRate = settings.getContingencyRate();
        BigDecimal defaultTaxRate = settings.getTaxRate();

        BigDecimal squareFeet = ns(project.getSquareFeet());
        BigDecimal materialPerSqFt = ns(project.getMaterialCostPerSqFt());

        // Skip square feet validation for WindowDoorReplace and DeckPatioAddition
        if (!(project instanceof WindowDoorReplace) && !(project instanceof DeckPatioAddition)) {
            if (squareFeet.compareTo(ZERO) <= 0) {
                throw new EstimationException("E101", "Square feet must be greater than zero");
            }
            if (materialPerSqFt.compareTo(ZERO) < 0) {
                throw new EstimationException("E102", "Material cost per sq ft cannot be negative");
            }
        }

        BigDecimal locationFactor = ns(project.getLocationFactor(), ONE);
        
        project.setLaborRate(laborRate);
        project.setOverheadRate(overheadRate);
        project.setContingencyRate(contingencyRate);
        
        // Use provided taxRate or default
        if (project.getTaxRate() == null) {
            project.setTaxRate(defaultTaxRate);
        }

        BigDecimal typeFactor = ONE;
        BigDecimal extraLaborPerStory = ZERO;
        BigDecimal insulationAdderPerSqFt = ZERO;
        BigDecimal pitchFactor = ONE;
        BigDecimal skylightCost = ZERO;
        BigDecimal tearOffCost = ZERO;

        if (project instanceof SidingReplace siding) {
            typeFactor = resolveSidingMaterialFactor(settings, siding.getSidingMaterial());

            int extraStories = Math.max(0, siding.getStories() - 1);
            extraLaborPerStory = laborRate.multiply(settings.getSidingExtraLaborPerStoryRate()
                    .multiply(BigDecimal.valueOf(extraStories)));

            if (siding.isIncludeInsulation()) {
                insulationAdderPerSqFt = settings.getInsulationAdderPerSqFt();
            }
        } else if (project instanceof RoofingReplace roofing) {
            typeFactor = resolveRoofMaterialFactor(settings, roofing.getRoofMaterial());

            int extraStories = Math.max(0, roofing.getStories() - 1);
            extraLaborPerStory = laborRate.multiply(settings.getRoofingExtraLaborPerStoryRate()
                    .multiply(BigDecimal.valueOf(extraStories)));

            pitchFactor = roofing.getRoofPitch().multiply(settings.getRoofPitchFactorPerUnit()).add(ONE);

            if (roofing.isTearOffRequired()) {
                tearOffCost = roofing.getAreaSqFt().multiply(settings.getRoofTearOffCostPerSqFt());
            }

            if (roofing.isHasSkylights()) {
                skylightCost = settings.getRoofSkylightCost()
                        .multiply(BigDecimal.valueOf(roofing.getNumSkylights()));
            }
        } else if (project instanceof KitchenRemodel kitchen) {
            Double applianceAllowanceValue = kitchen.getApplianceAllowance() != null ? kitchen.getApplianceAllowance() : 0.0;
            
            BigDecimal applianceCost = BigDecimal.valueOf(applianceAllowanceValue);
            
            // Material cost based on the average of cabinet, countertop, and flooring
            BigDecimal materialCost = ns(kitchen.getSquareFeet()).multiply(materialPerSqFt);

            BigDecimal baseCost = applianceCost.add(materialCost);

            if (kitchen.getPlumbingChanges() != null && kitchen.getPlumbingChanges()) {
                baseCost = baseCost.add(settings.getKitchenPlumbingCost());
            }

            if (kitchen.getElectricalChanges() != null && kitchen.getElectricalChanges()) {
                baseCost = baseCost.add(settings.getKitchenElectricalCost());
            }

            BigDecimal overhead = baseCost.multiply(overheadRate);
            BigDecimal contingency = baseCost.multiply(contingencyRate);

            BigDecimal estimatePrice = baseCost.add(overhead).add(contingency);
            BigDecimal taxAmount = estimatePrice.multiply(defaultTaxRate);
            BigDecimal totalPrice = estimatePrice.add(taxAmount);

            project.setEstimatePrice(round2(estimatePrice));
            project.setTaxAmount(round2(taxAmount));
            project.setTotalPrice(round2(totalPrice));

            log.debug("Calculated kitchen remodel estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                    project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());
            
            return project;
        } else if (project instanceof WindowDoorReplace windowDoor) {
            BigDecimal windowCostPerUnit = settings.getWindowBaseCostPerUnit();
            BigDecimal doorCostPerUnit = settings.getDoorBaseCostPerUnit();
            
            BigDecimal windowMaterialFactor = resolveWindowMaterialFactor(settings, windowDoor.getWindowType());
            BigDecimal doorMaterialFactor = resolveDoorMaterialFactor(settings, windowDoor.getDoorType());
            
            BigDecimal windowMaterialCost = BigDecimal.valueOf(windowDoor.getWindowCount()).multiply(windowCostPerUnit).multiply(windowMaterialFactor);
            BigDecimal doorMaterialCost = BigDecimal.valueOf(windowDoor.getDoorCount()).multiply(doorCostPerUnit).multiply(doorMaterialFactor);
            
            // 3 labor rates per window and per door
            BigDecimal windowLaborCost = BigDecimal.valueOf(windowDoor.getWindowCount())
                    .multiply(laborRate.multiply(settings.getWindowDoorLaborRateMultiplier()));
            BigDecimal doorLaborCost = BigDecimal.valueOf(windowDoor.getDoorCount())
                    .multiply(laborRate.multiply(settings.getWindowDoorLaborRateMultiplier()));
            
            BigDecimal baseCost = windowMaterialCost.add(doorMaterialCost).add(windowLaborCost).add(doorLaborCost);
            
            BigDecimal overhead = baseCost.multiply(overheadRate);
            BigDecimal contingency = baseCost.multiply(contingencyRate);
            BigDecimal estimatePrice = baseCost.add(overhead).add(contingency);
            BigDecimal taxAmount = estimatePrice.multiply(project.getTaxRate());
            BigDecimal totalPrice = estimatePrice.add(taxAmount);
            
            project.setEstimatePrice(round2(estimatePrice));
            project.setTaxAmount(round2(taxAmount));
            project.setTotalPrice(round2(totalPrice));
            
            log.debug("Calculated window/door replacement estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                    project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());
            
            return project;
        } else if (project instanceof DeckPatioAddition deckPatio) {
            BigDecimal baseMaterialCostPerSqFt = settings.getDeckBaseMaterialCostPerSqFt();
            BigDecimal deckMaterialFactor = resolveDeckMaterialFactor(settings, deckPatio.getDeckMaterial());
            
            BigDecimal areaSqFt = BigDecimal.valueOf(deckPatio.getAreaSqFt() != null ? deckPatio.getAreaSqFt() : 0.0);
            
            // Material cost: areaSqFt × baseCost × materialFactor
            BigDecimal materialCostPerSqFt = baseMaterialCostPerSqFt.multiply(deckMaterialFactor);
            BigDecimal materialCost = areaSqFt.multiply(materialCostPerSqFt);
            // Expose per-sqft material cost back on the project so frontend can show breakdown
            deckPatio.setMaterialCostPerSqFt(round2(materialCostPerSqFt));
            
            // Labor cost: areaSqFt × laborRate
            BigDecimal laborCost = areaSqFt.multiply(laborRate);
            
            BigDecimal baseCost = materialCost.add(laborCost);
            
            // Add railing cost if applicable
            if (Boolean.TRUE.equals(deckPatio.getHasRailing())) {
                // Assume perimeter is approximately 4 × sqrt(area) for a squarish deck
                // Cost of $40 per linear foot for railing
                BigDecimal perimeter = BigDecimal.valueOf(Math.sqrt(areaSqFt.doubleValue()))
                        .setScale(2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(4));
                BigDecimal railingCost = perimeter.multiply(settings.getDeckRailingCostPerLinearFoot());
                baseCost = baseCost.add(railingCost);
            }
            
            // Add stairs cost
            if (deckPatio.getStairsCount() != null && deckPatio.getStairsCount() > 0) {
                BigDecimal stairsCost = BigDecimal.valueOf(deckPatio.getStairsCount())
                        .multiply(settings.getDeckStairsCost());
                baseCost = baseCost.add(stairsCost);
            }
            
            // Add covered cost if applicable
            if (Boolean.TRUE.equals(deckPatio.getIsCovered())) {
                BigDecimal coverCost = areaSqFt.multiply(settings.getDeckCoverCostPerSqFt());
                baseCost = baseCost.add(coverCost);
            }
            
            // Apply location factor
            BigDecimal locationFactorValue = ns(deckPatio.getLocationFactor(), ONE);
            baseCost = baseCost.multiply(locationFactorValue);
            
            BigDecimal overhead = baseCost.multiply(overheadRate);
            BigDecimal contingency = baseCost.multiply(contingencyRate);
            BigDecimal estimatePrice = baseCost.add(overhead).add(contingency);
            BigDecimal taxAmount = estimatePrice.multiply(project.getTaxRate());
            BigDecimal totalPrice = estimatePrice.add(taxAmount);
            
            project.setEstimatePrice(round2(estimatePrice));
            project.setTaxAmount(round2(taxAmount));
            project.setTotalPrice(round2(totalPrice));
            
            log.debug("Calculated deck/patio addition estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                    project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());
            
            return project;
        } else if (project instanceof FloorReplace floorReplace) {
            BigDecimal area = ns(floorReplace.getSquareFeet());
            BigDecimal baseMaterialCostPerSqFt = ns(floorReplace.getMaterialCostPerSqFt());
            
            BigDecimal newFloorFactor = resolveFlooringMaterialFactor(settings, floorReplace.getNewFloorMaterial());
            
            // Material cost: area × baseCost × materialFactor
            BigDecimal materialCost = area.multiply(baseMaterialCostPerSqFt).multiply(newFloorFactor);
            
            // Labor cost: area × laborRate
            BigDecimal laborCost = area.multiply(laborRate);
            
            BigDecimal baseCost = materialCost.add(laborCost);
            
            // Add subfloor repair cost if needed
            if (Boolean.TRUE.equals(floorReplace.getSubfloorRepairNeeded())) {
                BigDecimal subfloorRepairCost = area.multiply(settings.getFloorSubfloorRepairCostPerSqFt());
                baseCost = baseCost.add(subfloorRepairCost);
            }
            
            // Add removal cost for existing floor (varies by material)
            BigDecimal removalFactor = resolveFlooringRemovalFactor(settings, floorReplace.getExistingFloorMaterial());
            BigDecimal removalCost = area.multiply(settings.getFloorRemovalBaseCostPerSqFt()).multiply(removalFactor);
            baseCost = baseCost.add(removalCost);
            
            // Apply location factor
            BigDecimal locationFactorValue = ns(floorReplace.getLocationFactor(), ONE);
            baseCost = baseCost.multiply(locationFactorValue);
            
            BigDecimal overhead = baseCost.multiply(overheadRate);
            BigDecimal contingency = baseCost.multiply(contingencyRate);
            BigDecimal estimatePrice = baseCost.add(overhead).add(contingency);
            BigDecimal taxAmount = estimatePrice.multiply(project.getTaxRate());
            BigDecimal totalPrice = estimatePrice.add(taxAmount);
            
            project.setEstimatePrice(round2(estimatePrice));
            project.setTaxAmount(round2(taxAmount));
            project.setTotalPrice(round2(totalPrice));
            
            log.debug("Calculated floor replacement estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                    project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());
            
            return project;
        }

        BigDecimal effectiveLaborRate = laborRate.add(extraLaborPerStory);
        BigDecimal laborCost = squareFeet.multiply(effectiveLaborRate);
        BigDecimal materialCost = squareFeet.multiply(materialPerSqFt.add(insulationAdderPerSqFt)).multiply(typeFactor);

        BigDecimal baseCost = laborCost.add(materialCost).add(tearOffCost).add(skylightCost).multiply(locationFactor).multiply(pitchFactor);
        BigDecimal overhead = baseCost.multiply(overheadRate);
        BigDecimal contingency = baseCost.multiply(contingencyRate);

        BigDecimal estimatePrice = baseCost.add(overhead).add(contingency);
        BigDecimal taxAmount = estimatePrice.multiply(defaultTaxRate);
        BigDecimal totalPrice = estimatePrice.add(taxAmount);

        project.setEstimatePrice(round2(estimatePrice));
        project.setTaxAmount(round2(taxAmount));
        project.setTotalPrice(round2(totalPrice));

        log.debug("Calculated estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());

        return project;
    }

    private BigDecimal resolveSidingMaterialFactor(EstimateSettings settings, SidingMaterial material) {
        if (material == null) return ONE;
        return switch (material) {
            case VINYL -> settings.getSidingFactors().getVinyl();
            case WOOD -> settings.getSidingFactors().getWood();
            case FIBER_CEMENT -> settings.getSidingFactors().getFiberCement();
            case BRICK -> settings.getSidingFactors().getBrick();
            case STONE_VENEER -> settings.getSidingFactors().getStoneVeneer();
        };
    }

    private BigDecimal resolveRoofMaterialFactor(EstimateSettings settings, RoofMaterial material) {
        if (material == null) return ONE;
        return switch (material) {
            case ASPHALT -> settings.getRoofFactors().getAsphalt();
            case METAL -> settings.getRoofFactors().getMetal();
            case CLAY -> settings.getRoofFactors().getClay();
            case SLATE -> settings.getRoofFactors().getSlate();
            case SYNTHETIC -> settings.getRoofFactors().getSynthetic();
        };
    }

    private BigDecimal resolveWindowMaterialFactor(EstimateSettings settings, WindowType windowType) {
        if (windowType == null) return ONE;
        return switch (windowType) {
            case CASEMENT -> settings.getWindowFactors().getCasement();
            case SLIDER -> settings.getWindowFactors().getSlider();
            case DOUBLE_HUNG -> settings.getWindowFactors().getDoubleHung();
            case AWNING -> settings.getWindowFactors().getAwning();
            case FIXED -> settings.getWindowFactors().getFixed();
        };
    }

    private BigDecimal resolveDoorMaterialFactor(EstimateSettings settings, DoorType doorType) {
        if (doorType == null) return ONE;
        return switch (doorType) {
            case WOOD -> settings.getDoorFactors().getWood();
            case FIBERGLASS -> settings.getDoorFactors().getFiberglass();
            case STEEL -> settings.getDoorFactors().getSteel();
            case GLASS_PANEL -> settings.getDoorFactors().getGlassPanel();
        };
    }

    private BigDecimal resolveDeckMaterialFactor(EstimateSettings settings, DeckMaterial deckMaterial) {
        if (deckMaterial == null) return ONE;
        return switch (deckMaterial) {
            case WOOD -> settings.getDeckFactors().getWood();
            case COMPOSITE -> settings.getDeckFactors().getComposite();
            case PVC -> settings.getDeckFactors().getPvc();
            case ALUMINUM -> settings.getDeckFactors().getAluminum();
        };
    }

    private BigDecimal resolveFlooringMaterialFactor(EstimateSettings settings, FlooringMaterial flooringMaterial) {
        if (flooringMaterial == null) return ONE;
        return switch (flooringMaterial) {
            case HARDWOOD -> settings.getFlooringFactors().getHardwood();
            case ENGINEERED_HARDWOOD -> settings.getFlooringFactors().getEngineeredHardwood();
            case LAMINATE -> settings.getFlooringFactors().getLaminate();
            case VINYL -> settings.getFlooringFactors().getVinyl();
            case TILE -> settings.getFlooringFactors().getTile();
            case CARPET -> settings.getFlooringFactors().getCarpet();
            case POLISHED_CONCRETE -> settings.getFlooringFactors().getPolishedConcrete();
        };
    }

    private BigDecimal resolveFlooringRemovalFactor(EstimateSettings settings, FlooringMaterial flooringMaterial) {
        if (flooringMaterial == null) return ONE;
        return switch (flooringMaterial) {
            case HARDWOOD -> settings.getFlooringRemovalFactors().getHardwood();
            case ENGINEERED_HARDWOOD -> settings.getFlooringRemovalFactors().getEngineeredHardwood();
            case LAMINATE -> settings.getFlooringRemovalFactors().getLaminate();
            case VINYL -> settings.getFlooringRemovalFactors().getVinyl();
            case TILE -> settings.getFlooringRemovalFactors().getTile();
            case CARPET -> settings.getFlooringRemovalFactors().getCarpet();
            case POLISHED_CONCRETE -> settings.getFlooringRemovalFactors().getPolishedConcrete();
        };
    }

    private BigDecimal ns(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private BigDecimal ns(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }

    private BigDecimal round2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
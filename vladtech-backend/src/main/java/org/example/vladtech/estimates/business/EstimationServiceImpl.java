package org.example.vladtech.estimates.business;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class EstimationServiceImpl implements EstimationService {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Value("${renovation.rates.labor:50.00}")
    private BigDecimal laborRate;

    @Value("${renovation.rates.overhead:0.15}")
    private BigDecimal overheadRate;

    @Value("${renovation.rates.contingency:0.10}")
    private BigDecimal contingencyRate;

    @Value("${renovation.rates.tax:0.15}")
    private BigDecimal taxRate;

    // Siding material multipliers (defaults)
    @Value("${siding.material.factor.VINYL:1.00}")
    private BigDecimal vinylFactor;

    @Value("${siding.material.factor.WOOD:1.10}")
    private BigDecimal woodFactor;

    @Value("${siding.material.factor.FIBER_CEMENT:1.20}")
    private BigDecimal fiberCementFactor;

    @Value("${siding.material.factor.BRICK:1.30}")
    private BigDecimal brickFactor;

    @Value("${siding.material.factor.STONE_VENEER:1.45}")
    private BigDecimal stoneVeneerFactor;

    // Roofing material multipliers (defaults)
    @Value("${roof.material.factor.ASPHALT:1.00}")
    private BigDecimal asphaltFactor;

    @Value("${roof.material.factor.METAL:1.20}")
    private BigDecimal metalFactor;

    @Value("${roof.material.factor.CLAY:1.50}")
    private BigDecimal clayFactor;

    @Value("${roof.material.factor.SLATE:1.80}")
    private BigDecimal slateFactor;

    @Value("${roof.material.factor.SYNTHETIC:1.30}")
    private BigDecimal syntheticFactor;

    // Window material multipliers (defaults)
    @Value("${window.material.factor.CASEMENT:1.00}")
    private BigDecimal casementFactor;

    @Value("${window.material.factor.SLIDER:0.95}")
    private BigDecimal sliderFactor;

    @Value("${window.material.factor.DOUBLE_HUNG:1.05}")
    private BigDecimal doubleHungFactor;

    @Value("${window.material.factor.AWNING:1.10}")
    private BigDecimal awningFactor;

    @Value("${window.material.factor.FIXED:0.85}")
    private BigDecimal fixedFactor;

    // Door material multipliers (defaults)
    @Value("${door.material.factor.WOOD:1.00}")
    private BigDecimal woodDoorFactor;

    @Value("${door.material.factor.FIBERGLASS:1.15}")
    private BigDecimal fiberglassDoorFactor;

    @Value("${door.material.factor.STEEL:1.05}")
    private BigDecimal steelDoorFactor;

    @Value("${door.material.factor.GLASS_PANEL:1.30}")
    private BigDecimal glassPanelDoorFactor;

    // Deck material multipliers (defaults)
    @Value("${deck.material.factor.WOOD:1.00}")
    private BigDecimal woodDeckFactor;

    @Value("${deck.material.factor.COMPOSITE:1.25}")
    private BigDecimal compositeDeckFactor;

    @Value("${deck.material.factor.PVC:1.40}")
    private BigDecimal pvcDeckFactor;

    @Value("${deck.material.factor.ALUMINUM:1.50}")
    private BigDecimal aluminumDeckFactor;

    // Flooring material multipliers (defaults)
    @Value("${flooring.material.factor.HARDWOOD:1.00}")
    private BigDecimal hardwoodFloorFactor;

    @Value("${flooring.material.factor.ENGINEERED_HARDWOOD:0.85}")
    private BigDecimal engineeredHardwoodFloorFactor;

    @Value("${flooring.material.factor.LAMINATE:0.60}")
    private BigDecimal laminateFloorFactor;

    @Value("${flooring.material.factor.VINYL:0.50}")
    private BigDecimal vinylFloorFactor;

    @Value("${flooring.material.factor.TILE:0.90}")
    private BigDecimal tileFloorFactor;

    @Value("${flooring.material.factor.CARPET:0.70}")
    private BigDecimal carpetFloorFactor;

    @Value("${flooring.material.factor.POLISHED_CONCRETE:0.95}")
    private BigDecimal polishedConcreteFloorFactor;

    @Override
    public RenovationProject calculateEstimate(RenovationProject project) {
        if (project == null) {
            throw new EstimationException("E000", "Project cannot be null");
        }

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
            project.setTaxRate(taxRate);
        }

        BigDecimal typeFactor = ONE;
        BigDecimal extraLaborPerStory = ZERO;
        BigDecimal insulationAdderPerSqFt = ZERO;
        BigDecimal pitchFactor = ONE;
        BigDecimal skylightCost = ZERO;
        BigDecimal tearOffCost = ZERO;

        if (project instanceof SidingReplace siding) {
            typeFactor = resolveSidingMaterialFactor(siding.getSidingMaterial());

            int extraStories = Math.max(0, siding.getStories() - 1);
            extraLaborPerStory = laborRate.multiply(BigDecimal.valueOf(0.10 * extraStories));

            if (siding.isIncludeInsulation()) {
                insulationAdderPerSqFt = BigDecimal.valueOf(0.75);
            }
        } else if (project instanceof RoofingReplace roofing) {
            typeFactor = resolveRoofMaterialFactor(roofing.getRoofMaterial());

            int extraStories = Math.max(0, roofing.getStories() - 1);
            extraLaborPerStory = laborRate.multiply(BigDecimal.valueOf(0.15 * extraStories));

            pitchFactor = roofing.getRoofPitch().multiply(BigDecimal.valueOf(0.1)).add(ONE);

            if (roofing.isTearOffRequired()) {
                tearOffCost = roofing.getAreaSqFt().multiply(BigDecimal.valueOf(1.50));
            }

            if (roofing.isHasSkylights()) {
                skylightCost = BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(roofing.getNumSkylights()));
            }
        } else if (project instanceof KitchenRemodel kitchen) {
            Double applianceAllowanceValue = kitchen.getApplianceAllowance() != null ? kitchen.getApplianceAllowance() : 0.0;
            
            BigDecimal applianceCost = BigDecimal.valueOf(applianceAllowanceValue);
            
            // Material cost based on the average of cabinet, countertop, and flooring
            BigDecimal materialCost = ns(kitchen.getSquareFeet()).multiply(materialPerSqFt);

            BigDecimal baseCost = applianceCost.add(materialCost);

            if (kitchen.getPlumbingChanges() != null && kitchen.getPlumbingChanges()) {
                baseCost = baseCost.add(BigDecimal.valueOf(2000)); // Example plumbing cost
            }

            if (kitchen.getElectricalChanges() != null && kitchen.getElectricalChanges()) {
                baseCost = baseCost.add(BigDecimal.valueOf(1500)); // Example electrical cost
            }

            BigDecimal overhead = baseCost.multiply(overheadRate);
            BigDecimal contingency = baseCost.multiply(contingencyRate);

            BigDecimal estimatePrice = baseCost.add(overhead).add(contingency);
            BigDecimal taxAmount = estimatePrice.multiply(taxRate);
            BigDecimal totalPrice = estimatePrice.add(taxAmount);

            project.setEstimatePrice(round2(estimatePrice));
            project.setTaxAmount(round2(taxAmount));
            project.setTotalPrice(round2(totalPrice));

            log.debug("Calculated kitchen remodel estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                    project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());
            
            return project;
        } else if (project instanceof WindowDoorReplace windowDoor) {
            BigDecimal windowCostPerUnit = BigDecimal.valueOf(800); // Base cost per window
            BigDecimal doorCostPerUnit = BigDecimal.valueOf(1200); // Base cost per door
            
            BigDecimal windowMaterialFactor = resolveWindowMaterialFactor(windowDoor.getWindowType());
            BigDecimal doorMaterialFactor = resolveDoorMaterialFactor(windowDoor.getDoorType());
            
            BigDecimal windowMaterialCost = BigDecimal.valueOf(windowDoor.getWindowCount()).multiply(windowCostPerUnit).multiply(windowMaterialFactor);
            BigDecimal doorMaterialCost = BigDecimal.valueOf(windowDoor.getDoorCount()).multiply(doorCostPerUnit).multiply(doorMaterialFactor);
            
            // 3 labor rates per window and per door
            BigDecimal windowLaborCost = BigDecimal.valueOf(windowDoor.getWindowCount()).multiply(laborRate.multiply(BigDecimal.valueOf(3)));
            BigDecimal doorLaborCost = BigDecimal.valueOf(windowDoor.getDoorCount()).multiply(laborRate.multiply(BigDecimal.valueOf(3)));
            
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
            BigDecimal baseMaterialCostPerSqFt = BigDecimal.valueOf(25.00); // Base deck material cost per sq ft
            BigDecimal deckMaterialFactor = resolveDeckMaterialFactor(deckPatio.getDeckMaterial());
            
            BigDecimal areaSqFt = BigDecimal.valueOf(deckPatio.getAreaSqFt() != null ? deckPatio.getAreaSqFt() : 0.0);
            
            // Material cost: areaSqFt × baseCost × materialFactor
            BigDecimal materialCost = areaSqFt.multiply(baseMaterialCostPerSqFt).multiply(deckMaterialFactor);
            
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
                BigDecimal railingCost = perimeter.multiply(BigDecimal.valueOf(40));
                baseCost = baseCost.add(railingCost);
            }
            
            // Add stairs cost
            if (deckPatio.getStairsCount() != null && deckPatio.getStairsCount() > 0) {
                BigDecimal stairsCost = BigDecimal.valueOf(deckPatio.getStairsCount()).multiply(BigDecimal.valueOf(500));
                baseCost = baseCost.add(stairsCost);
            }
            
            // Add covered cost if applicable
            if (Boolean.TRUE.equals(deckPatio.getIsCovered())) {
                BigDecimal coverCost = areaSqFt.multiply(BigDecimal.valueOf(15)); // $15 per sq ft for roof covering
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
            
            BigDecimal newFloorFactor = resolveFlooringMaterialFactor(floorReplace.getNewFloorMaterial());
            
            // Material cost: area × baseCost × materialFactor
            BigDecimal materialCost = area.multiply(baseMaterialCostPerSqFt).multiply(newFloorFactor);
            
            // Labor cost: area × laborRate
            BigDecimal laborCost = area.multiply(laborRate);
            
            BigDecimal baseCost = materialCost.add(laborCost);
            
            // Add subfloor repair cost if needed
            if (Boolean.TRUE.equals(floorReplace.getSubfloorRepairNeeded())) {
                BigDecimal subfloorRepairCost = area.multiply(BigDecimal.valueOf(3.50)); // $3.50 per sq ft for subfloor repair
                baseCost = baseCost.add(subfloorRepairCost);
            }
            
            // Add removal cost for existing floor (varies by material)
            BigDecimal removalFactor = resolveFlooringRemovalFactor(floorReplace.getExistingFloorMaterial());
            BigDecimal removalCost = area.multiply(BigDecimal.valueOf(2.00)).multiply(removalFactor); // Base $2/sqft × factor
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
        BigDecimal taxAmount = estimatePrice.multiply(taxRate);
        BigDecimal totalPrice = estimatePrice.add(taxAmount);

        project.setEstimatePrice(round2(estimatePrice));
        project.setTaxAmount(round2(taxAmount));
        project.setTotalPrice(round2(totalPrice));

        log.debug("Calculated estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());

        return project;
    }

    private BigDecimal resolveSidingMaterialFactor(SidingMaterial material) {
        if (material == null) return ONE;
        return switch (material) {
            case VINYL -> vinylFactor;
            case WOOD -> woodFactor;
            case FIBER_CEMENT -> fiberCementFactor;
            case BRICK -> brickFactor;
            case STONE_VENEER -> stoneVeneerFactor;
        };
    }

    private BigDecimal resolveRoofMaterialFactor(RoofMaterial material) {
        if (material == null) return ONE;
        return switch (material) {
            case ASPHALT -> asphaltFactor;
            case METAL -> metalFactor;
            case CLAY -> clayFactor;
            case SLATE -> slateFactor;
            case SYNTHETIC -> syntheticFactor;
        };
    }

    private BigDecimal resolveWindowMaterialFactor(WindowType windowType) {
        if (windowType == null) return ONE;
        return switch (windowType) {
            case CASEMENT -> casementFactor;
            case SLIDER -> sliderFactor;
            case DOUBLE_HUNG -> doubleHungFactor;
            case AWNING -> awningFactor;
            case FIXED -> fixedFactor;
        };
    }

    private BigDecimal resolveDoorMaterialFactor(DoorType doorType) {
        if (doorType == null) return ONE;
        return switch (doorType) {
            case WOOD -> woodDoorFactor;
            case FIBERGLASS -> fiberglassDoorFactor;
            case STEEL -> steelDoorFactor;
            case GLASS_PANEL -> glassPanelDoorFactor;
        };
    }

    private BigDecimal resolveDeckMaterialFactor(DeckMaterial deckMaterial) {
        if (deckMaterial == null) return ONE;
        return switch (deckMaterial) {
            case WOOD -> woodDeckFactor;
            case COMPOSITE -> compositeDeckFactor;
            case PVC -> pvcDeckFactor;
            case ALUMINUM -> aluminumDeckFactor;
        };
    }

    private BigDecimal resolveFlooringMaterialFactor(FlooringMaterial flooringMaterial) {
        if (flooringMaterial == null) return ONE;
        return switch (flooringMaterial) {
            case HARDWOOD -> hardwoodFloorFactor;
            case ENGINEERED_HARDWOOD -> engineeredHardwoodFloorFactor;
            case LAMINATE -> laminateFloorFactor;
            case VINYL -> vinylFloorFactor;
            case TILE -> tileFloorFactor;
            case CARPET -> carpetFloorFactor;
            case POLISHED_CONCRETE -> polishedConcreteFloorFactor;
        };
    }

    private BigDecimal resolveFlooringRemovalFactor(FlooringMaterial flooringMaterial) {
        if (flooringMaterial == null) return ONE;
        return switch (flooringMaterial) {
            case HARDWOOD -> BigDecimal.valueOf(1.20); // Harder to remove
            case ENGINEERED_HARDWOOD -> BigDecimal.valueOf(1.10);
            case LAMINATE -> BigDecimal.valueOf(0.80); // Easier to remove
            case VINYL -> BigDecimal.valueOf(0.70); // Easiest
            case TILE -> BigDecimal.valueOf(1.50); // Most difficult
            case CARPET -> BigDecimal.valueOf(0.60); // Very easy
            case POLISHED_CONCRETE -> BigDecimal.valueOf(0.50); // Minimal removal
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
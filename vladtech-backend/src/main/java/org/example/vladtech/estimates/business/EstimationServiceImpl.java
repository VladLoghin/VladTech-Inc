package org.example.vladtech.estimates.business;

import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.data.siding.SidingReplace;
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

    @Override
    public RenovationProject calculateEstimate(RenovationProject project) {
        if (project == null) {
            throw new EstimationException("E000", "Project cannot be null");
        }

        BigDecimal squareFeet = ns(project.getSquareFeet());
        BigDecimal materialPerSqFt = ns(project.getMaterialCostPerSqFt());
        BigDecimal locationFactor = ns(project.getLocationFactor(), ONE);

        if (squareFeet.compareTo(ZERO) <= 0) {
            throw new EstimationException("E101", "Square feet must be greater than zero");
        }
        if (materialPerSqFt.compareTo(ZERO) < 0) {
            throw new EstimationException("E102", "Material cost per sq ft cannot be negative");
        }

        project.setLaborRate(laborRate);
        project.setOverheadRate(overheadRate);
        project.setContingencyRate(contingencyRate);
        project.setTaxRate(taxRate);

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
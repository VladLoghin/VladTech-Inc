// java
package org.example.vladtech.estimates.business;

import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.data.RenovationProject;
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

        if (project instanceof SidingReplace siding) {
            typeFactor = resolveMaterialFactor(siding.getSidingMaterial());

            int extraStories = Math.max(0, siding.getStories() - 1);
            extraLaborPerStory = laborRate.multiply(BigDecimal.valueOf(0.10 * extraStories));

            if (siding.isIncludeInsulation()) {
                insulationAdderPerSqFt = BigDecimal.valueOf(0.75);
            }
        }
        // Future project types can be handled with additional instanceof branches.

        BigDecimal effectiveLaborRate = laborRate.add(extraLaborPerStory);
        BigDecimal laborCost = squareFeet.multiply(effectiveLaborRate);
        BigDecimal materialCost = squareFeet.multiply(materialPerSqFt.add(insulationAdderPerSqFt)).multiply(typeFactor);

        BigDecimal baseCost = laborCost.add(materialCost).multiply(locationFactor);
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

    private BigDecimal resolveMaterialFactor(SidingMaterial material) {
        if (material == null) return ONE;
        return switch (material) {
            case VINYL -> vinylFactor;
            case WOOD -> woodFactor;
            case FIBER_CEMENT -> fiberCementFactor;
            case BRICK -> brickFactor;
            case STONE_VENEER -> stoneVeneerFactor;
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
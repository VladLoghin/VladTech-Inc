package org.example.vladtech.estimates.business;

import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.data.RenovationProject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class EstimationServiceImpl implements EstimationService {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    // Configurable rates with defaults
    @Value("${renovation.rates.labor:50.00}")
    private BigDecimal laborRate;

    @Value("${renovation.rates.overhead:0.15}")
    private BigDecimal overheadRate;

    @Value("${renovation.rates.contingency:0.10}")
    private BigDecimal contingencyRate;

    @Override
    public RenovationProject calculateEstimate(RenovationProject project) {
        if (project == null) {
            throw new IllegalArgumentException("project cannot be null");
        }

        BigDecimal squareFeet         = ns(project.getSquareFeet());
        BigDecimal materialPerSqFt    = ns(project.getMaterialCostPerSqFt());
        BigDecimal locationFactor     = ns(project.getLocationFactor(), ONE);
        BigDecimal taxRate            = ns(project.getTaxRate());

        // Set configured rates
        project.setLaborRate(laborRate);
        project.setOverheadRate(overheadRate);
        project.setContingencyRate(contingencyRate);

        BigDecimal laborCost      = squareFeet.multiply(laborRate);
        BigDecimal materialCost   = squareFeet.multiply(materialPerSqFt);
        BigDecimal baseCost       = laborCost.add(materialCost).multiply(locationFactor);
        BigDecimal overhead       = baseCost.multiply(overheadRate);
        BigDecimal contingency    = baseCost.multiply(contingencyRate);

        BigDecimal estimatePrice  = baseCost.add(overhead).add(contingency);
        BigDecimal taxAmount      = estimatePrice.multiply(taxRate);
        BigDecimal totalPrice     = estimatePrice.add(taxAmount);

        project.setEstimatePrice(round2(estimatePrice));
        project.setTaxAmount(round2(taxAmount));
        project.setTotalPrice(round2(totalPrice));

        log.debug("Calculated estimate: estimatePrice={}, taxAmount={}, totalPrice={}",
                project.getEstimatePrice(), project.getTaxAmount(), project.getTotalPrice());

        return project;
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
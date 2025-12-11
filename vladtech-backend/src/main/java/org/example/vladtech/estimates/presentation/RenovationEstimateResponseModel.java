package org.example.vladtech.estimates.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenovationEstimateResponseModel {

    // Echo inputs
    private BigDecimal squareFeet;
    private BigDecimal laborRate;
    private BigDecimal materialCostPerSqFt;
    private BigDecimal overheadRate;
    private BigDecimal contingencyRate;
    private BigDecimal locationFactor;
    private BigDecimal taxRate;

    // Calculated outputs
    private BigDecimal estimatePrice;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
}
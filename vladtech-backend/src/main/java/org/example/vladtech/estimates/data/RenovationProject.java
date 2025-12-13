package org.example.vladtech.estimates.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenovationProject {

    // Inputs
    private BigDecimal squareFeet;
    private BigDecimal laborRate;
    private BigDecimal materialCostPerSqFt;
    private BigDecimal overheadRate;
    private BigDecimal contingencyRate;
    private BigDecimal locationFactor;
    private BigDecimal taxRate;

    // Derived values
    private BigDecimal estimatePrice;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
}

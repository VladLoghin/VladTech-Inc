package org.example.vladtech.estimates.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenovationEstimateRequestModel {

    @NotNull(message = "Square feet is required")
    @Positive(message = "Square feet must be positive")
    private BigDecimal squareFeet;

    @NotNull(message = "Material cost per sq ft is required")
    @PositiveOrZero(message = "Material cost must be non-negative")
    private BigDecimal materialCostPerSqFt;

    @Positive(message = "Location factor must be positive")
    private BigDecimal locationFactor;
}
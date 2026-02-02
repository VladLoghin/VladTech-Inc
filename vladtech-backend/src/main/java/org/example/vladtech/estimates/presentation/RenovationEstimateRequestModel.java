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

    // Siding-specific properties
    @Positive(message = "Stories must be positive")
    private Integer stories;

    private Boolean includeInsulation;

    private String sidingMaterial;

    // Roofing-specific properties
    @Positive(message = "Roof pitch must be positive")
    private BigDecimal roofPitch;

    private String roofMaterial;

    private Boolean tearOffRequired;

    private Boolean hasSkylights;

    @PositiveOrZero(message = "Number of skylights must be non-negative")
    private Integer numSkylights;

    // Kitchen-specific properties
    @PositiveOrZero(message = "Appliance allowance must be non-negative")
    private Double applianceAllowance;

    private Boolean plumbingChanges;

    private Boolean electricalChanges;

    private String flooringMaterial;

    private String cabinetQuality;

    private String countertopMaterial;

    // Window and Door-specific properties
    private String windowType;

    private String doorType;

    @PositiveOrZero(message = "Window count must be non-negative")
    private Integer windowCount;

    @PositiveOrZero(message = "Door count must be non-negative")
    private Integer doorCount;

    // Deck/Patio-specific properties
    private String deckMaterial;

    private Boolean hasRailing;

    @PositiveOrZero(message = "Stairs count must be non-negative")
    private Integer stairsCount;

    private Boolean isCovered;

    @PositiveOrZero(message = "Area square feet must be non-negative")
    private Double areaSqFt;

    // Floor-specific properties
    private String existingFloorMaterial;

    private String newFloorMaterial;

    private Boolean subfloorRepairNeeded;
}
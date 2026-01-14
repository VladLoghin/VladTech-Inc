package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateResponseMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class RenovationEstimateController {

    private final EstimationService estimationService;

    // Resolve multiple beans: use the MapStruct-generated impl or whichever you prefer.
    // Alternatively, annotate that implementation with @Primary.
    @Qualifier("renovationEstimateResponseMapperImpl")
    private final RenovationEstimateResponseMapper responseMapper;

    @GetMapping("/calculate")
    public ResponseEntity<RenovationEstimateResponseModel> calculateEstimate(
            @RequestParam String projectType,
            @RequestParam(required = false) BigDecimal squareFeet,
            @RequestParam(required = false) BigDecimal areaSqFt,
            @RequestParam BigDecimal materialCostPerSqFt,
            @RequestParam(required = false, defaultValue = "1.00") BigDecimal locationFactor,
            @RequestParam(required = false) BigDecimal taxRate,
            // Siding-specific
            @RequestParam(required = false) Integer stories,
            @RequestParam(required = false) Boolean includeInsulation,
            @RequestParam(required = false) SidingMaterial sidingMaterial,
            @RequestParam(required = false) String lang // optional, ignored by backend calc
    ) {
        // Basic validation shared across project types
        BigDecimal sqft = squareFeet != null ? squareFeet : areaSqFt;
        if (sqft == null || sqft.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Square feet must be positive");
        }
        if (materialCostPerSqFt == null || materialCostPerSqFt.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Material cost per sq ft must be non-negative");
        }
        if (locationFactor != null && locationFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Location factor must be positive");
        }

        log.info("Received estimate calculation: type={}, sqFt={}, lang={}", projectType, sqft, lang);

        RenovationProject project;

        if ("SIDING_REPLACE".equalsIgnoreCase(projectType)) {
            SidingReplace siding = new SidingReplace();
            siding.setAreaSqFt(sqft); // internally may set parent squareFeet
            siding.setMaterialCostPerSqFt(materialCostPerSqFt);
            siding.setLocationFactor(locationFactor);
            siding.setTaxRate(taxRate);

            // Apply siding-specific inputs with sensible defaults
            siding.setStories(stories != null ? stories : 1);
            siding.setIncludeInsulation(Boolean.TRUE.equals(includeInsulation));
            siding.setSidingMaterial(sidingMaterial); // may be null; service can default if needed

            project = siding;
        } else {
            RenovationProject base = new RenovationProject();
            base.setSquareFeet(sqft);
            base.setMaterialCostPerSqFt(materialCostPerSqFt);
            base.setLocationFactor(locationFactor);
            base.setTaxRate(taxRate);
            project = base;
        }

        RenovationProject calculated = estimationService.calculateEstimate(project);
        RenovationEstimateResponseModel response = responseMapper.toResponse(calculated);

        log.info("Estimate calculated: totalPrice={}", response.getTotalPrice());
        return ResponseEntity.ok(response);
    }
}
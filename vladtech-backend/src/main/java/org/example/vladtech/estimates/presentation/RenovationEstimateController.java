package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class RenovationEstimateController {

    private final EstimationService estimationService;
    private final RenovationEstimateResponseMapper responseMapper;

    @GetMapping("/calculate")
    public ResponseEntity<RenovationEstimateResponseModel> calculateEstimate(
            @RequestParam BigDecimal squareFeet,
            @RequestParam BigDecimal materialCostPerSqFt,
            @RequestParam(required = false, defaultValue = "1") BigDecimal locationFactor,
            @RequestParam(required = false) BigDecimal taxRate) {

        if (squareFeet == null || squareFeet.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Square feet must be positive");
        }
        if (materialCostPerSqFt == null || materialCostPerSqFt.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Material cost per sq ft must be non-negative");
        }
        if (locationFactor != null && locationFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Location factor must be positive");
        }

        log.info("Received estimate calculation request for {} sq ft", squareFeet);

        RenovationProject project = new RenovationProject();
        project.setSquareFeet(squareFeet);
        project.setMaterialCostPerSqFt(materialCostPerSqFt);
        project.setLocationFactor(locationFactor);
        project.setTaxRate(taxRate);

        RenovationProject calculated = estimationService.calculateEstimate(project);
        RenovationEstimateResponseModel response = responseMapper.toResponse(calculated);

        log.info("Estimate calculated successfully: totalPrice={}", response.getTotalPrice());

        return ResponseEntity.ok(response);
    }
}
package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.CabinetQuality;
import org.example.vladtech.estimates.data.kitchen.CountertopMaterial;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.shared.FlooringMaterial;
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
            // Roofing-specific
            @RequestParam(required = false) BigDecimal roofPitch,
            @RequestParam(required = false) RoofMaterial roofMaterial,
            // Kitchen-specific
            @RequestParam(required = false) Double applianceAllowance,
            @RequestParam(required = false) Boolean plumbingChanges,
            @RequestParam(required = false) Boolean electricalChanges,
            @RequestParam(required = false) String flooringMaterial,
            @RequestParam(required = false) String cabinetQuality,
            @RequestParam(required = false) String countertopMaterial,
            @RequestParam(required = false) String lang
    ) {
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
            siding.setAreaSqFt(sqft);
            siding.setMaterialCostPerSqFt(materialCostPerSqFt);
            siding.setLocationFactor(locationFactor);
            siding.setTaxRate(taxRate);
            siding.setStories(stories != null ? stories : 1);
            siding.setIncludeInsulation(Boolean.TRUE.equals(includeInsulation));
            siding.setSidingMaterial(sidingMaterial);
            project = siding;
        } else if ("ROOFING_REPLACE".equalsIgnoreCase(projectType)) {
            RoofingReplace roofing = new RoofingReplace();
            roofing.setAreaSqFt(sqft);
            roofing.setMaterialCostPerSqFt(materialCostPerSqFt);
            roofing.setLocationFactor(locationFactor);
            roofing.setTaxRate(taxRate);
            roofing.setRoofPitch(roofPitch != null ? roofPitch : BigDecimal.ONE);
            roofing.setRoofMaterial(roofMaterial);
            project = roofing;
        } else if ("KITCHEN_REMODEL".equalsIgnoreCase(projectType)) {
            KitchenRemodel kitchen = new KitchenRemodel();
            kitchen.setAreaSqFt(sqft);
            kitchen.setMaterialCostPerSqFt(materialCostPerSqFt);
            kitchen.setLocationFactor(locationFactor);
            kitchen.setTaxRate(taxRate);
            kitchen.setApplianceAllowance(applianceAllowance);
            kitchen.setPlumbingChanges(plumbingChanges);
            kitchen.setElectricalChanges(electricalChanges);
            kitchen.setFlooringMaterial(flooringMaterial != null ? FlooringMaterial.valueOf(flooringMaterial) : null);
            kitchen.setCabinetQuality(cabinetQuality != null ? CabinetQuality.valueOf(cabinetQuality) : null);
            kitchen.setCountertopMaterial(countertopMaterial != null ? CountertopMaterial.valueOf(countertopMaterial) : null);
            project = kitchen;
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
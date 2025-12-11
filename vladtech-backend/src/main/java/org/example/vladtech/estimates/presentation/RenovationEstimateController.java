package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateRequestMapper;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class RenovationEstimateController {

    private final EstimationService estimationService;
    private final RenovationEstimateRequestMapper requestMapper;
    private final RenovationEstimateResponseMapper responseMapper;

    @PostMapping("/calculate")
    public ResponseEntity<RenovationEstimateResponseModel> calculateEstimate(
            @Validated @RequestBody RenovationEstimateRequestModel request) {

        log.info("Received estimate calculation request for {} sq ft", request.getSquareFeet());

        RenovationProject project = requestMapper.toEntity(request);
        RenovationProject calculated = estimationService.calculateEstimate(project);
        RenovationEstimateResponseModel response = responseMapper.toResponse(calculated);

        log.info("Estimate calculated successfully: totalPrice={}", response.getTotalPrice());

        return ResponseEntity.ok(response);
    }
}
package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.estimates.business.EstimateSettingsService;
import org.example.vladtech.estimates.data.EstimateSettings;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estimates/config")
@RequiredArgsConstructor
public class EstimateSettingsController {

    private final EstimateSettingsService estimateSettingsService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping
    public ResponseEntity<EstimateSettings> getSettings() {
        return ResponseEntity.ok(estimateSettingsService.getSettings());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping
    public ResponseEntity<EstimateSettings> updateSettings(@RequestBody EstimateSettings settings) {
        return ResponseEntity.ok(estimateSettingsService.updateSettings(settings));
    }
}

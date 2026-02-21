package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.estimates.data.Estimate;
import org.example.vladtech.estimates.data.EstimateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateRepository estimateRepository;

    @PostMapping
    public ResponseEntity<Estimate> createEstimate(@RequestBody Estimate estimate,
                                                   @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        estimate.setOwnerAuth0Id(userId);
        estimate.setCreatedAt(Instant.now());
        Estimate saved = estimateRepository.save(estimate);
        return ResponseEntity.created(URI.create("/api/estimates/" + saved.getEstimateId())).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Estimate>> listEstimates(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        List<Estimate> all = estimateRepository.findByOwnerAuth0Id(userId);
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estimate> getEstimate(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        Optional<Estimate> maybe = estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, userId);
        return maybe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estimate> updateEstimate(@PathVariable String id,
                                                   @RequestBody Estimate update,
                                                   @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        return estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, userId).map(existing -> {
            update.setEstimateId(existing.getEstimateId());
            update.setOwnerAuth0Id(existing.getOwnerAuth0Id());
            update.setCreatedAt(existing.getCreatedAt());
            Estimate saved = estimateRepository.save(update);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.status(403).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstimate(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        Optional<Estimate> maybe = estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, userId);
        if (maybe.isPresent()) {
            estimateRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(403).build();
    }
}

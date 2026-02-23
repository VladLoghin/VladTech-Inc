package org.example.vladtech.estimates.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.estimates.data.Estimate;
import org.example.vladtech.estimates.data.EstimateRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

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

    

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> getEstimatePdf(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        Optional<Estimate> maybe = estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, userId);
        if (maybe.isEmpty()) return ResponseEntity.status(403).build();

        Path pdfPath = Paths.get("uploads", "estimates", id + ".pdf");
        if (!Files.exists(pdfPath)) return ResponseEntity.notFound().build();

        try {
            byte[] data = Files.readAllBytes(pdfPath);
            ByteArrayResource resource = new ByteArrayResource(data);

            // Use the saved estimate title as the download filename when possible
            String title = maybe.get().getTitle();
            if (title == null || title.isBlank()) {
                title = "estimate-" + id;
            }
            // sanitize filename: allow letters, numbers, space, dash, underscore, dot
            // keep regex simple to avoid Java string escape issues
            String safe = title.replaceAll("[^a-zA-Z0-9 _.-]", "_");
            if (safe.length() > 120) safe = safe.substring(0, 120);
            String filename = safe + ".pdf";

                return ResponseEntity.ok()
                    .contentLength(data.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "/{id}/upload-pdf", consumes = {
            "multipart/form-data"
    })
    public ResponseEntity<Estimate> uploadEstimatePdf(@PathVariable String id,
                                                      @RequestParam("file") MultipartFile file,
                                                      @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaimAsString("sub");
        Optional<Estimate> maybe = estimateRepository.findByEstimateIdAndOwnerAuth0Id(id, userId);
        if (maybe.isEmpty()) return ResponseEntity.status(403).build();

        Estimate e = maybe.get();
        Path uploadDir = Paths.get("uploads", "estimates");
        try {
            Files.createDirectories(uploadDir);
            Path outFile = uploadDir.resolve(id + ".pdf");

            // Allow overwriting existing PDF for this estimate. Write the uploaded bytes
            // and update the saved estimate record accordingly.
            Files.write(outFile, file.getBytes());
            e.setPdfUrl("/api/estimates/" + id + "/pdf");
            estimateRepository.save(e);
            return ResponseEntity.ok(e);
        } catch (IOException ex) {
            return ResponseEntity.status(500).build();
        }
    }
}

package org.example.vladtech.reviews.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.reviews.business.ReviewService;
import org.example.vladtech.reviews.business.ReviewServiceImpl;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasAnyAuthority('Admin', 'Employee')")
    @GetMapping()
    public ResponseEntity<List<ReviewResponseModel>> getAllReviews(@AuthenticationPrincipal Jwt jwt,
                                                                   @RequestParam(required = false) String clientName,
                                                                   @RequestParam(required = false) Rating rating
                                                                   ) {
        return ResponseEntity.ok(reviewService.getAllReviews(clientName, rating));
    }



    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/{reviewId}/visibility")
    public ResponseEntity<ReviewResponseModel> patchReviewVisibility(
            @PathVariable String reviewId,
            @RequestBody ReviewRequestModel reviewRequestModel
    ) {
        if (reviewId == null || reviewId.isBlank() || reviewRequestModel == null || reviewRequestModel.getVisible() == null) {
            return ResponseEntity.badRequest().build();
        }


        return ResponseEntity.ok(
                reviewService.updateReviewVisibility(reviewId, reviewRequestModel.getVisible())
        );
    }


    @GetMapping("/visible")
    public ResponseEntity<List<ReviewResponseModel>> getAllVisibleReviews(@RequestParam(required = false) String clientName,
                                                                           @RequestParam(required = false) Rating rating) {
        return ResponseEntity.ok(reviewService.getAllVisibleReviews(clientName, rating));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Client')")
    public ResponseEntity<ReviewResponseModel> createReview(
            @Valid @RequestPart("review") ReviewRequestModel reviewRequest,
            @RequestPart(value = "photos", required = false) MultipartFile[] photos,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getClaim("sub");
        reviewRequest.setClientId(userId);
        reviewRequest.setVisible(false);
        return ResponseEntity.ok(reviewService.createReview(reviewRequest, photos, userId));
    }

    @PreAuthorize("hasAuthority('Client')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId,
                                             @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getClaimAsString("sub") == null) {
            return ResponseEntity.status(401).build();
        }
        String userId = jwt.getClaim("sub");
        if (reviewId == null || reviewId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        reviewService.deleteReviewAsClient(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('Client')")
    @GetMapping("/mine")
    public ResponseEntity<List<ReviewResponseModel>> getMyReviews(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getClaim("sub");
        return ResponseEntity.ok(reviewService.getReviewsByOwnerAuth0Id(userId));
    }

    @GetMapping("/satisfaction-percentage")
    public ResponseEntity<Double> getSatisfactionPercentage() {
        if (reviewService.getAllVisibleReviews(null, null).isEmpty()) {
            return ResponseEntity.ok(0.0);
        }
        return ResponseEntity.ok(reviewService.computeSatisfactionPercentage());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/admin/{reviewId}")
    public void deleteReviewsByReviewId(@PathVariable String reviewId) {
        if (reviewId == null || reviewId.isBlank()) {
            throw new IllegalArgumentException("Review ID must not be null or blank");
        }

        try {
            reviewService.deleteReview(reviewId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}


package org.example.vladtech.reviews.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.reviews.business.ReviewService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasAnyAuthority('Admin', 'Employee')")
    @GetMapping()
    public ResponseEntity<List<ReviewResponseModel>> getAllReviews(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }



    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/{reviewId}/visibility")
    public ResponseEntity<ReviewResponseModel> patchReviewVisibility(
            @PathVariable String reviewId,
            @RequestBody ReviewRequestModel reviewRequestModel
    ) {
        return ResponseEntity.ok(
                reviewService.updateReviewVisibility(reviewId, reviewRequestModel.getVisible())
        );
    }


    @GetMapping("/visible")
    public ResponseEntity<List<ReviewResponseModel>> getAllVisibleReviews() {
        return ResponseEntity.ok(reviewService.getAllVisibleReviews());
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
        String userId = jwt.getClaim("sub");
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

}


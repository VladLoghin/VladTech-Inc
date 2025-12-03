package org.example.vladtech.reviews.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.reviews.business.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponseModel>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/id/{reviewId}")
    public ResponseEntity<ReviewResponseModel> getReviewById(@PathVariable String reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @GetMapping("/visible")
    public ResponseEntity<List<ReviewResponseModel>> getAllVisibleReviews() {
        return ResponseEntity.ok(reviewService.getAllVisibleReviews());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReviewResponseModel>> getReviewsByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(reviewService.getReviewsByClient(clientId));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<ReviewResponseModel>> getReviewsByAppointment(@PathVariable String appointmentId) {
        return ResponseEntity.ok(reviewService.getReviewsByAppointment(appointmentId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponseModel> createReview(@RequestBody ReviewRequestModel reviewRequest) {
        return ResponseEntity.ok(reviewService.createReview(reviewRequest));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseModel> updateReview(
            @PathVariable String reviewId,
            @RequestBody ReviewRequestModel reviewRequest
    ) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, reviewRequest));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}

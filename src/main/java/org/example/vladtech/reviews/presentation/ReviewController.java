package org.example.vladtech.reviews.presentation;

import org.example.vladtech.reviews.business.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Get all reviews
    @GetMapping
    public ResponseEntity<List<ReviewResponseModel>> getAllReviews() {
        List<ReviewResponseModel> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // Get a review by its ID
    @GetMapping("/id/{reviewId}")
    public ResponseEntity<ReviewResponseModel> getReviewById(@PathVariable String reviewId) {
        ReviewResponseModel review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    // Get all visible reviews
    @GetMapping("/visible")
    public ResponseEntity<List<ReviewResponseModel>> getAllVisibleReviews() {
        List<ReviewResponseModel> reviews = reviewService.getAllVisibleReviews();
        return ResponseEntity.ok(reviews);
    }

    // Get reviews by client ID
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReviewResponseModel>> getReviewsByClient(@PathVariable String clientId) {
        List<ReviewResponseModel> reviews = reviewService.getReviewsByClient(clientId);
        return ResponseEntity.ok(reviews);
    }

    // Get reviews by appointment ID
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<ReviewResponseModel>> getReviewsByAppointment(@PathVariable String appointmentId) {
        List<ReviewResponseModel> reviews = reviewService.getReviewsByAppointment(appointmentId);
        return ResponseEntity.ok(reviews);
    }

    // Create a new review
    @PostMapping
    public ResponseEntity<ReviewResponseModel> createReview(@RequestBody ReviewResponseModel reviewRequest) {
        ReviewResponseModel created = reviewService.createReview(reviewRequest);
        return ResponseEntity.ok(created);
    }

    // Update an existing review
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseModel> updateReview(
            @PathVariable String reviewId,
            @RequestBody ReviewResponseModel reviewRequest
    ) {
        ReviewResponseModel updated = reviewService.updateReview(reviewId, reviewRequest);
        return ResponseEntity.ok(updated);
    }

    // Delete a review by ID
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}

package org.example.vladtech.reviews.business;

import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import java.util.List;

public interface ReviewService {

    // Create a new review
    ReviewResponseModel createReview(ReviewResponseModel reviewRequest);

    // Get a review by its ID
    ReviewResponseModel getReviewById(String reviewId);

    // Get all reviews
    List<ReviewResponseModel> getAllReviews();

    // Get all visible reviews
    List<ReviewResponseModel> getAllVisibleReviews();

    // Get reviews for a specific client
    List<ReviewResponseModel> getReviewsByClient(String clientId);

    // Get reviews for a specific appointment
    List<ReviewResponseModel> getReviewsByAppointment(String appointmentId);

    // Update an existing review
    ReviewResponseModel updateReview(String reviewId, ReviewResponseModel reviewRequest);

    // Delete a review by ID
    void deleteReview(String reviewId);
}

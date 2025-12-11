package org.example.vladtech.reviews.business;

import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    // Create a new review
    ReviewResponseModel createReview(ReviewRequestModel request, MultipartFile[] photos);

    List<ReviewResponseModel> getAllReviews();

    ReviewResponseModel updateReviewVisibility(String reviewId, boolean visible);

    ReviewResponseModel GetReviewById(String reviewId);
    /*
        // Get a review by its ID
        ReviewResponseModel getReviewById(String reviewId);

        // Get all reviews
        List<ReviewResponseModel> getAllReviews();
    */
    // Get all visible reviews
    List<ReviewResponseModel> getAllVisibleReviews();
/*
    // Get reviews for a specific client
    List<ReviewResponseModel> getReviewsByClient(String clientId);

    // Get reviews for a specific appointment
    List<ReviewResponseModel> getReviewsByAppointment(String appointmentId);

    // Update an existing review
    ReviewResponseModel updateReview(String reviewId, ReviewRequestModel reviewRequest);

    // Delete a review by ID
    void deleteReview(String reviewId);
    */
}


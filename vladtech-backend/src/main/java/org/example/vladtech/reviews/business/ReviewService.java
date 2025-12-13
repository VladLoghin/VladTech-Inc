package org.example.vladtech.reviews.business;

import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    // Create a new review
    ReviewResponseModel createReview(ReviewRequestModel request, MultipartFile[] photos, String OwnerAuth0Id);

    List<ReviewResponseModel> getAllReviews();

    ReviewResponseModel updateReviewVisibility(String reviewId, boolean visible);

    ReviewResponseModel GetReviewById(String reviewId);

    ReviewResponseModel deleteReviewAsClient(String reviewId, String clientId);

    List<ReviewResponseModel> getAllVisibleReviews();

    List<ReviewResponseModel> getReviewsByOwnerAuth0Id(String ownerAuth0Id);

}


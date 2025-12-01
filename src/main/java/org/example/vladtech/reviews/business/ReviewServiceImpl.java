package org.example.vladtech.reviews.business;

import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public ReviewResponseModel createReview(ReviewResponseModel reviewRequest) {
        // map ReviewResponseModel -> Review
        Review review = new Review();
        review.setClientId(reviewRequest.getClientId());
        review.setAppointmentId(reviewRequest.getAppointmentId());
        review.setComment(reviewRequest.getComment());
        review.setVisible(reviewRequest.getVisible());
        review.setRating(reviewRequest.getRating());
        review.setPhotos(reviewRequest.getPhotos());

        Review saved = reviewRepository.save(review);

        // map back to ReviewResponseModel
        return mapToResponse(saved);
    }

    @Override
    public ReviewResponseModel getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponseModel> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ReviewResponseModel> getAllVisibleReviews() {
        return reviewRepository.findByVisibleTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ReviewResponseModel> getReviewsByClient(String clientId) {
        return reviewRepository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ReviewResponseModel> getReviewsByAppointment(String appointmentId) {
        return reviewRepository.findByAppointmentId(appointmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ReviewResponseModel updateReview(String reviewId, ReviewResponseModel reviewRequest) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        existing.setComment(reviewRequest.getComment());
        existing.setVisible(reviewRequest.getVisible());
        existing.setRating(reviewRequest.getRating());
        existing.setPhotos(reviewRequest.getPhotos());

        Review updated = reviewRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private ReviewResponseModel mapToResponse(Review review) {
        return new ReviewResponseModel(
                review.getReviewId(),
                review.getClientId(),
                review.getAppointmentId(),
                review.getComment(),
                review.getVisible(),
                review.getRating(),
                review.getPhotos()
        );
    }
}

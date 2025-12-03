package org.example.vladtech.reviews.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.reviews.mapperlayer.ReviewRequestMapper;
import org.example.vladtech.reviews.mapperlayer.ReviewResponseMapper;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRequestMapper requestMapper;
    private final ReviewResponseMapper responseMapper;

    @Override
    public ReviewResponseModel createReview(ReviewRequestModel reviewRequest) {
        Review review = requestMapper.requestModelToEntity(reviewRequest);
        Review saved = reviewRepository.save(review);
        return responseMapper.entityToResponseModel(saved);
    }

    @Override
    public ReviewResponseModel getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return responseMapper.entityToResponseModel(review);
    }

    @Override
    public List<ReviewResponseModel> getAllReviews() {
        return responseMapper.entityListToResponseModelList(reviewRepository.findAll());
    }

    @Override
    public List<ReviewResponseModel> getAllVisibleReviews() {
        return responseMapper.entityListToResponseModelList(reviewRepository.findByVisibleTrue());
    }

    @Override
    public List<ReviewResponseModel> getReviewsByClient(String clientId) {
        return responseMapper.entityListToResponseModelList(reviewRepository.findByClientId(clientId));
    }

    @Override
    public List<ReviewResponseModel> getReviewsByAppointment(String appointmentId) {
        return responseMapper.entityListToResponseModelList(reviewRepository.findByAppointmentId(appointmentId));
    }

    @Override
    public ReviewResponseModel updateReview(String reviewId, ReviewRequestModel reviewRequest) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // map fields from request to existing entity
        requestMapper.requestModelToEntity(reviewRequest); // returns a new entity
        // copy non-ID fields
        existing.setClientId(reviewRequest.getClientId());
        existing.setAppointmentId(reviewRequest.getAppointmentId());
        existing.setComment(reviewRequest.getComment());
        existing.setVisible(reviewRequest.getVisible());
        existing.setRating(reviewRequest.getRating());
        existing.setPhotos(reviewRequest.getPhotos());

        Review updated = reviewRepository.save(existing);
        return responseMapper.entityToResponseModel(updated);
    }

    @Override
    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}

package org.example.vladtech.reviews.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.filestorageservice.FileStorageService;
import org.example.vladtech.reviews.mapperlayer.ReviewRequestMapper;
import org.example.vladtech.reviews.mapperlayer.ReviewResponseMapper;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRequestMapper requestMapper;
    private final ReviewResponseMapper responseMapper;
    private final FileStorageService fileStorageService;

    @Override
    public List<ReviewResponseModel> getAllReviews() {
        return responseMapper.entityListToResponseModelList(reviewRepository.findAll());
    }


    @Override
    public ReviewResponseModel createReview(ReviewRequestModel reviewRequest, MultipartFile[] photos) {
        Review review = requestMapper.requestModelToEntity(reviewRequest);

        if (photos != null) {
            List<Photo> photoList = Arrays.stream(photos)
                    .map(file -> {
                        try {
                            String filename = fileStorageService.save(file);
                            return new Photo(reviewRequest.getClientId(), filename, file.getContentType(), "/uploads/reviews/" + filename);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to save photo", e);
                        }
                    })

                    .collect(Collectors.toList());

            review.setPhotos(photoList);
        }

        Review saved = reviewRepository.save(review);
        return responseMapper.entityToResponseModel(saved);
    }

    @Override
    public ReviewResponseModel updateReviewVisibility(String reviewId, boolean visible) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        existing.setVisible(visible);
        return responseMapper.entityToResponseModel(reviewRepository.save(existing));
    }



    @Override
    public ReviewResponseModel GetReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return responseMapper.entityToResponseModel(review);
    }

    /*
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
    */
    @Override
    public List<ReviewResponseModel> getAllVisibleReviews() {
        return responseMapper.entityListToResponseModelList(reviewRepository.findByVisibleTrue());
    }
/*
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
 */
}


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
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ReviewResponseModel createReview(ReviewRequestModel reviewRequest, MultipartFile[] photos, String OwnerAuth0Id) {
        Review review = requestMapper.requestModelToEntity(reviewRequest);

        review.setOwnerAuth0Id(OwnerAuth0Id);

        review.setClientId(reviewRequest.getClientId());
        review.setClientName(reviewRequest.getClientName());
        review.setVisible(reviewRequest.getVisible());
        review.setRating(reviewRequest.getRating());
        review.setOwnerAuth0Id(OwnerAuth0Id);

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


    @Override
    public List<ReviewResponseModel> getAllVisibleReviews() {
        return responseMapper.entityListToResponseModelList(reviewRepository.findByVisibleTrue());
    }

    @PreAuthorize("hasAuthority('Client')")
    @Override
    public ReviewResponseModel deleteReviewAsClient(String reviewId, String clientId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        System.out.println("Delete reviewId = " + reviewId);
        if (!existing.getClientId().equals(clientId)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }

        reviewRepository.delete(existing);
        return responseMapper.entityToResponseModel(existing);
    }

    @Override
    public List<ReviewResponseModel> getReviewsByOwnerAuth0Id(String ownerAuth0Id) {
        return responseMapper.entityListToResponseModelList(reviewRepository.findByOwnerAuth0Id(ownerAuth0Id));
    }
}


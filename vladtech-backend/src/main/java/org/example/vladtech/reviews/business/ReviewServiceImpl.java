package org.example.vladtech.reviews.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.portfolio.business.PortfolioServiceImpl;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.example.vladtech.portfolio.mapperlayer.PortfolioMapper;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
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
import org.example.vladtech.portfolio.data.PortfolioItem;

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
    private final PortfolioServiceImpl portfolioService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    @Override
    public List<ReviewResponseModel> getAllReviews(String clientName, Rating ratingValue, String type) {

        boolean hasName = clientName != null && !clientName.isBlank();
        boolean hasType = type != null && !type.isBlank();
        boolean hasRating = ratingValue != null;

        List<Review> reviews;

        if (hasType && hasName && hasRating) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(
                            type, clientName, ratingValue
                    );

        } else if (hasType && hasName) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(type, clientName);

        } else if (hasType && hasRating) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndRating(type, ratingValue);

        } else if (hasName && hasRating) {
            reviews = reviewRepository
                    .findByClientNameContainingIgnoreCaseAndRating(clientName, ratingValue);

        } else if (hasType) {
            reviews = reviewRepository.findByTypeContainingIgnoreCase(type);

        } else if (hasName) {
            reviews = reviewRepository.findByClientNameContainingIgnoreCase(clientName);

        } else if (hasRating) {
            reviews = reviewRepository.findByRating(ratingValue);

        } else {
            reviews = reviewRepository.findAll();
        }

        return responseMapper.entityListToResponseModelList(reviews);
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
        review.setType(reviewRequest.getType());

        if (photos != null) {
            List<Photo> photoList = Arrays.stream(photos)
                    .map(file -> {
                        try {
                            String filename = fileStorageService.save(file);
                            return new Photo(reviewRequest.getClientId(), filename, file.getContentType(), "/api/uploads/reviews/" + filename);
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
    public List<ReviewResponseModel> getAllVisibleReviews(String clientName, Rating ratingValue, String type) {

        boolean hasName = clientName != null && !clientName.isBlank();
        boolean hasType = type != null && !type.isBlank();
        boolean hasRating = ratingValue != null;

        List<Review> reviews;

        if (hasType && hasName && hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(
                            type, clientName, ratingValue
                    );

        } else if (hasType && hasName) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(type, clientName);

        } else if (hasType && hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndRating(type, ratingValue);

        } else if (hasName && hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndClientNameContainingIgnoreCaseAndRating(clientName, ratingValue);

        } else if (hasType) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCase(type);

        } else if (hasName) {
            reviews = reviewRepository
                    .findByVisibleTrueAndClientNameContainingIgnoreCase(clientName);

        } else if (hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndRating(ratingValue);

        } else {
            reviews = reviewRepository.findByVisibleTrue();
        }

        return responseMapper.entityListToResponseModelList(reviews);
    }



    //@PreAuthorize("hasAuthority('Client')")
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

    @Override
    public double computeSatisfactionPercentage() {
        long count = reviewRepository.countByVisibleTrue();
        if (count == 0) {
            return 0.0;
        }

        List<Review> visibleReviews = reviewRepository.findByVisibleTrue();
        int totalStars = visibleReviews.stream()
                .mapToInt(review -> review.getRating().getValue())
                .sum();

        double averageRating = totalStars / (double) count;
        return (averageRating / 5.0) * 100.0;
    }

    @Override
    public void deleteReview(String reviewId) {
        boolean exists = reviewRepository.existsById(reviewId);
        if (!exists) {
            throw new RuntimeException("Review with ID " + reviewId + " does not exist");
        }
        reviewRepository.deleteReviewByReviewId(reviewId);
    }

    @Override
    public PortfolioResponseDto sendToPortfolio(String reviewId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (existing.isSentToPortfolio()) {
            throw new RuntimeException("Review has already been sent to portfolio");
        }

        String title = "Review by " + existing.getClientName();
        String imageUrl = existing.getPhotos().isEmpty()
                ? ""
                : existing.getPhotos().get(0).getUrl();
        String type = existing.getType();

        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setPortfolioId(java.util.UUID.randomUUID().toString());
        portfolioItem.setReviewId(reviewId);
        portfolioItem.setReviewerName(existing.getClientName());
        portfolioItem.setTitle(title);
        portfolioItem.setImageUrl(imageUrl);
        portfolioItem.setComments(new java.util.ArrayList<>());
        portfolioItem.setType(type);

        PortfolioItem savedItem = portfolioRepository.save(portfolioItem);

        existing.setSentToPortfolio(true);
        reviewRepository.save(existing);

        return portfolioMapper.entityToResponseDto(savedItem);
    }


    @Override
    public void resetPortfolioStatus(String reviewId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        existing.setSentToPortfolio(false);
        reviewRepository.save(existing);
    }
}


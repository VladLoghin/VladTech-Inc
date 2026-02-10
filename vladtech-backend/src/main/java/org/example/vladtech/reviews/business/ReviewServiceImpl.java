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
import org.example.vladtech.filestorageservice.IFileStorageService;
import org.example.vladtech.reviews.mapperlayer.ReviewRequestMapper;
import org.example.vladtech.reviews.mapperlayer.ReviewResponseMapper;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.auth.dataaccess.UserProfile;
import org.example.vladtech.auth.dataaccess.UserProfileRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRequestMapper requestMapper;
    private final ReviewResponseMapper responseMapper;
    private final IFileStorageService fileStorageService;
    private final PortfolioServiceImpl portfolioService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;
    private final UserProfileRepository userProfileRepository;

    @Override
    public List<ReviewResponseModel> getAllReviews(String clientName, Rating ratingValue, String type, String comment) {

        boolean hasName = clientName != null && !clientName.isBlank();
        boolean hasType = type != null && !type.isBlank();
        boolean hasRating = ratingValue != null;
        boolean hasComment = comment != null && !comment.isBlank();

        List<Review> reviews;

        // 4 filters
        if (hasType && hasName && hasRating && hasComment) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                            type, clientName, ratingValue, comment
                    );

            // 3 filters
        } else if (hasType && hasName && hasRating) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(
                            type, clientName, ratingValue
                    );

        } else if (hasType && hasName && hasComment) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                            type, clientName, comment
                    );

        } else if (hasType && hasRating && hasComment) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                            type, ratingValue, comment
                    );

        } else if (hasName && hasRating && hasComment) {
            reviews = reviewRepository
                    .findByClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                            clientName, ratingValue, comment
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

        } else if (hasType && hasComment) {
            reviews = reviewRepository
                    .findByTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(type, comment);

        } else if (hasName && hasComment) {
            reviews = reviewRepository
                    .findByClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(clientName, comment);

        } else if (hasRating && hasComment) {
            reviews = reviewRepository
                    .findByRatingAndCommentContainingIgnoreCase(ratingValue, comment);

            // 1 filter
        } else if (hasType) {
            reviews = reviewRepository.findByTypeContainingIgnoreCase(type);

        } else if (hasName) {
            reviews = reviewRepository.findByClientNameContainingIgnoreCase(clientName);

        } else if (hasRating) {
            reviews = reviewRepository.findByRating(ratingValue);

        } else if (hasComment) {
            reviews = reviewRepository.findByCommentContainingIgnoreCase(comment);

            // no filters
        } else {
            reviews = reviewRepository.findAll();
        }

        return responseMapper.entityListToResponseModelList(reviews);
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

    @Override
    public ReviewResponseModel createReview(ReviewRequestModel request, MultipartFile[] photos, String ownerAuth0Id) {
        enforceReviewBan(ownerAuth0Id);
        // Validate project ID is provided
        if (request.getProjectId() == null || request.getProjectId().isBlank()) {
            throw new RuntimeException("Project ID is required");
        }

        // Check if review already exists for this project and client
        boolean reviewExists = reviewRepository.existsByProjectIdAndClientId(
                request.getProjectId(),
                request.getClientId()
        );

        if (reviewExists) {
            throw new RuntimeException("A review for this project already exists. You cannot create another review for the same project.");
        }

        Review review = requestMapper.requestModelToEntity(request);

        review.setOwnerAuth0Id(ownerAuth0Id);
        review.setProjectId(request.getProjectId());  // SET PROJECT ID
        review.setClientId(request.getClientId());
        review.setClientName(request.getClientName());
        review.setVisible(request.getVisible());
        review.setRating(request.getRating());
        review.setOwnerAuth0Id(ownerAuth0Id);
        review.setType(request.getType());

        if (photos != null) {
            List<Photo> photoList = Arrays.stream(photos)
                    .map(file -> {
                        try {
                            String filename = fileStorageService.save(file);
                            return new Photo(request.getClientId(), filename, file.getContentType(), "/api/uploads/reviews/" + filename);
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
    public List<ReviewResponseModel> getAllVisibleReviews(
            String clientName,
            Rating ratingValue,
            String type,
            String comment
    ) {
        boolean hasName = clientName != null && !clientName.isBlank();
        boolean hasType = type != null && !type.isBlank();
        boolean hasRating = ratingValue != null;
        boolean hasComment = comment != null && !comment.isBlank();

        List<Review> reviews;

        // 4 filters
        if (hasType && hasName && hasRating && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                            type, clientName, ratingValue, comment
                    );

            // 3 filters (including comment)
        } else if (hasType && hasName && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                            type, clientName, comment
                    );

        } else if (hasType && hasRating && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                            type, ratingValue, comment
                    );

        } else if (hasName && hasRating && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                            clientName, ratingValue, comment
                    );

            // 3 filters (without comment) - your existing cases
        } else if (hasType && hasName && hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(
                            type, clientName, ratingValue
                    );

            // 2 filters (including comment)
        } else if (hasType && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(
                            type, comment
                    );

        } else if (hasName && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                            clientName, comment
                    );

        } else if (hasRating && hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndRatingAndCommentContainingIgnoreCase(
                            ratingValue, comment
                    );

            // 2 filters (your existing cases)
        } else if (hasType && hasName) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(
                            type, clientName
                    );

        } else if (hasType && hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCaseAndRating(
                            type, ratingValue
                    );

        } else if (hasName && hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndClientNameContainingIgnoreCaseAndRating(
                            clientName, ratingValue
                    );

            // 1 filter (including comment)
        } else if (hasComment) {
            reviews = reviewRepository
                    .findByVisibleTrueAndCommentContainingIgnoreCase(comment);

            // 1 filter (your existing cases)
        } else if (hasType) {
            reviews = reviewRepository
                    .findByVisibleTrueAndTypeContainingIgnoreCase(type);

        } else if (hasName) {
            reviews = reviewRepository
                    .findByVisibleTrueAndClientNameContainingIgnoreCase(clientName);

        } else if (hasRating) {
            reviews = reviewRepository
                    .findByVisibleTrueAndRating(ratingValue);

            // no filters
        } else {
            reviews = reviewRepository.findByVisibleTrue();
        }

        return responseMapper.entityListToResponseModelList(reviews);
    }




    @Override
    public ReviewResponseModel deleteReviewAsClient(String reviewId, String clientId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        System.out.println("Delete reviewId = " + reviewId);
        if (!existing.getClientId().equals(clientId)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }

        cleanupReviewPhotos(existing);
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
    public Map<String, Object> deleteReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        String ownerId = review.getOwnerAuth0Id() != null ? review.getOwnerAuth0Id() : review.getClientId();
        applyReviewStrike(ownerId);

        cleanupReviewPhotos(review);
        reviewRepository.delete(review);

        UserProfile profile = userProfileRepository.findUserProfileByAuth0Sub(ownerId);
        Map<String, Object> banInfo = new HashMap<>();
        if (profile != null) {
            banInfo.put("strikes", profile.getDeletedReviewCount());
            banInfo.put("permanent", profile.isReviewPermanentlyBanned());
            banInfo.put("banUntil", profile.getReviewBanUntil());
        }
        return banInfo;
    }

    @Override
    public void enforceReviewBan(String auth0Sub) {
        UserProfile profile = userProfileRepository.findUserProfileByAuth0Sub(auth0Sub);
        if (profile == null) return;

        if (profile.isReviewPermanentlyBanned()) {
            throw new IllegalStateException("You are permanently banned from posting reviews.");
        }

        Instant banUntil = profile.getReviewBanUntil();
        if (banUntil != null && banUntil.isAfter(Instant.now())) {
            throw new IllegalStateException("You are temporarily banned from posting reviews until " + banUntil + ".");
        }
    }

    @Override
    public void applyReviewStrike(String auth0Sub) {
        if (auth0Sub == null || auth0Sub.isBlank()) return;

        UserProfile profile = userProfileRepository.findUserProfileByAuth0Sub(auth0Sub);
        if (profile == null) {
            profile = UserProfile.builder().auth0Sub(auth0Sub).build();
        }

        int strikes = profile.getDeletedReviewCount() + 1;
        profile.setDeletedReviewCount(strikes);

        if (strikes == 1) {
            profile.setReviewBanUntil(Instant.now().plus(1, ChronoUnit.DAYS));
            profile.setReviewPermanentlyBanned(false);
        } else if (strikes == 2) {
            profile.setReviewBanUntil(Instant.now().plus(2, ChronoUnit.DAYS));
            profile.setReviewPermanentlyBanned(false);
        } else if (strikes >= 3) {
            profile.setReviewBanUntil(null);
            profile.setReviewPermanentlyBanned(true);
        }

        userProfileRepository.save(profile);
    }

    private void cleanupReviewPhotos(Review review) {
        if (review.getPhotos() != null && !review.getPhotos().isEmpty()) {
            for (Photo photo : review.getPhotos()) {
                if (photo.getFilename() != null && !photo.getFilename().isEmpty()) {
                    try {
                        fileStorageService.delete(photo.getFilename());
                        log.info("Deleted review photo from storage: {}", photo.getFilename());
                    } catch (Exception e) {
                        log.error("Failed to delete review photo {}: {}", photo.getFilename(), e.getMessage());
                    }
                }
            }
        }
    }
}


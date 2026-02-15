package org.example.vladtech.reviews.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    // Get all visible reviews
    List<Review> findByVisibleTrue();

    // Count visible reviews
    long countByVisibleTrue();

    List<Review> findByOwnerAuth0Id(String ownerAuth0Id);

    //Filtering queries (using RatingIn for range support)

    // Single-filter
    List<Review> findByClientNameContainingIgnoreCase(String clientName);
    List<Review> findByTypeContainingIgnoreCase(String type);
    List<Review> findByRatingIn(List<Rating> ratings);
    List<Review> findByCommentContainingIgnoreCase(String comment);

    // Two-filters
    List<Review> findByClientNameContainingIgnoreCaseAndRatingIn(String clientName, List<Rating> ratings);
    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(String type, String clientName);
    List<Review> findByTypeContainingIgnoreCaseAndRatingIn(String type, List<Rating> ratings);
    List<Review> findByClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(String clientName, String comment);
    List<Review> findByTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(String type, String comment);
    List<Review> findByRatingInAndCommentContainingIgnoreCase(List<Rating> ratings, String comment);

    // Three-filters
    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingIn(String type, String clientName, List<Rating> ratings);
    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(String type, String clientName, String comment);
    List<Review> findByTypeContainingIgnoreCaseAndRatingInAndCommentContainingIgnoreCase(String type, List<Rating> ratings, String comment);
    List<Review> findByClientNameContainingIgnoreCaseAndRatingInAndCommentContainingIgnoreCase(String clientName, List<Rating> ratings, String comment);

    // Four-filters
    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingInAndCommentContainingIgnoreCase(String type, String clientName, List<Rating> ratings, String comment);

    // Visible + single-filter
    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCase(String clientName);
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCase(String type);
    List<Review> findByVisibleTrueAndRatingIn(List<Rating> ratings);
    List<Review> findByVisibleTrueAndCommentContainingIgnoreCase(String comment);

    // Visible + two-filters
    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndRatingIn(String clientName, List<Rating> ratings);
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(String type, String clientName);
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndRatingIn(String type, List<Rating> ratings);
    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(String clientName, String comment);
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(String type, String comment);
    List<Review> findByVisibleTrueAndRatingInAndCommentContainingIgnoreCase(List<Rating> ratings, String comment);

    // Visible + three-filters
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingIn(String type, String clientName, List<Rating> ratings);
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(String type, String clientName, String comment);
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndRatingInAndCommentContainingIgnoreCase(String type, List<Rating> ratings, String comment);
    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndRatingInAndCommentContainingIgnoreCase(String clientName, List<Rating> ratings, String comment);

    // Visible + four-filters
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingInAndCommentContainingIgnoreCase(String type, String clientName, List<Rating> ratings, String comment);

    boolean existsByProjectIdAndClientId(String projectId, String clientId);

    List<Review> findByProjectIdAndClientId(String projectId, String clientId);

    List<Review> findByProjectIdAndClientIdAndAppointmentId(String projectId, String clientId, String appointmentId);

    void deleteReviewByReviewId(String reviewId);
}

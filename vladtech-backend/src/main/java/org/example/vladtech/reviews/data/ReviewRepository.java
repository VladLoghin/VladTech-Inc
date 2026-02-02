package org.example.vladtech.reviews.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    // Get all visible reviews
    List<Review> findByVisibleTrue();

    // Count visible reviews
    long countByVisibleTrue();

    List<Review> findByOwnerAuth0Id(String ownerAuth0Id);

    List<Review> findByClientNameAndRating(String clientName, Rating rating);

    //Filtering queries
    List<Review> findByClientNameContainingIgnoreCase(String clientName);

    List<Review> findByClientNameContainingIgnoreCaseAndRating(String clientName, Rating rating);

    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCase(String clientName);

    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndRating(String clientName, Rating rating);

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCase(String type);

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameAndRating(String type, String clientName, Rating rating);
    List<Review> findByTypeContainingIgnoreCaseAndClientNameAndRating(String type, String clientName, Rating rating);

    List<Review> findByTypeContainingIgnoreCase(String type);

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndRating(String type, Rating rating);

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(String type, String clientName);

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(String type, String clientName, Rating rating);

    List<Review> findByTypeContainingIgnoreCaseAndRating(String type, Rating rating);

    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(String type, String clientName);

    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(String type, String clientName, Rating rating);


    // Single-filter
    List<Review> findByCommentContainingIgnoreCase(String comment);

    // Two-filters
    List<Review> findByClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(String clientName, String comment);
    List<Review> findByTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(String type, String comment);
    List<Review> findByRatingAndCommentContainingIgnoreCase(Rating rating, String comment);

    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(String type, String clientName, String comment);

    List<Review> findByTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(String type, Rating rating, String comment);

    List<Review> findByClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(String clientName, Rating rating, String comment);

    List<Review> findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(String type, String clientName, Rating rating, String comment);

    // Visible + comment only
    List<Review> findByVisibleTrueAndCommentContainingIgnoreCase(String comment);

    // Visible + (name/type/rating) + comment
    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
            String clientName, String comment
    );

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(
            String type, String comment
    );

    List<Review> findByVisibleTrueAndRatingAndCommentContainingIgnoreCase(
            Rating rating, String comment
    );

    // Visible + 3 filters including comment
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
            String type, String clientName, String comment
    );

    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
            String type, Rating rating, String comment
    );

    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
            String clientName, Rating rating, String comment
    );

    // Visible + all 4 filters
    List<Review> findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
            String type, String clientName, Rating rating, String comment
    );

    //

    List<Review> findByVisibleTrueAndRating(Rating rating);

    List<Review> findByRating(Rating rating);

    void deleteReviewByReviewId(String reviewId);
//
//    // Get reviews by client ID
//    List<Review> findByClientId(String clientId);
//
//    // Get reviews by appointment ID
//    List<Review> findByAppointmentId(String appointmentId);
}


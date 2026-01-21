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

    List<Review> findByClientNameContainingIgnoreCase(String clientName);

    List<Review> findByClientNameContainingIgnoreCaseAndRating(String clientName, Rating rating);

    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCase(String clientName);

    List<Review> findByVisibleTrueAndClientNameContainingIgnoreCaseAndRating(String clientName, Rating rating);

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


package org.example.vladtech.reviews.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    // Get all visible reviews
    List<Review> findByVisibleTrue();

    List<Review> findByOwnerAuth0Id(String ownerAuth0Id);

//
//    // Get reviews by client ID
//    List<Review> findByClientId(String clientId);
//
//    // Get reviews by appointment ID
//    List<Review> findByAppointmentId(String appointmentId);
}


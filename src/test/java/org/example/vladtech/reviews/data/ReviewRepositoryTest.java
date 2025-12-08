package org.example.vladtech.reviews.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanup() {
        reviewRepository.deleteAll();
    }

    @Test
    void findByVisibleTrue_returnsOnlyVisibleReviews() {
        // Arrange
        Review visibleReview = new Review("c1", "a1", "visible comment", true, Rating.FIVE);
        Review hiddenReview = new Review("c2", "a2", "hidden comment", false, Rating.ONE);

        reviewRepository.saveAll(List.of(visibleReview, hiddenReview));

        // Act
        List<Review> visibleReviews = reviewRepository.findByVisibleTrue();

        // Assert
        assertEquals(1, visibleReviews.size(), "Should return only one visible review");
        Review result = visibleReviews.get(0);
        assertEquals("visible comment", result.getComment());
        assertTrue(result.getVisible(), "Returned review must be visible");
    }
}

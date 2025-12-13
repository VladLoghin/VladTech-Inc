package org.example.vladtech.reviews.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewRepositoryTest {

    private ReviewRepository reviewRepository;

    @BeforeEach
    void setup() {
        reviewRepository = mock(ReviewRepository.class);
    }

    @Test
    void findByVisibleTrue_returnsOnlyVisibleReviews() {
        Review visibleReview = new Review("c1", "a1", "Ronnie", "visible comment", true, Rating.FIVE);
        Review hiddenReview = new Review("c2", "a2", "James", "hidden comment", false, Rating.ONE);

        when(reviewRepository.findByVisibleTrue()).thenReturn(List.of(visibleReview));

        List<Review> visibleReviews = reviewRepository.findByVisibleTrue();

        assertEquals(1, visibleReviews.size());
        assertEquals("visible comment", visibleReviews.get(0).getComment());
        assertTrue(visibleReviews.get(0).getVisible());
    }
}

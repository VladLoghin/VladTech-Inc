package org.example.vladtech.reviews.business;

import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.reviews.mapperlayer.ReviewRequestMapper;
import org.example.vladtech.reviews.mapperlayer.ReviewResponseMapper;
import org.example.vladtech.filestorageservice.IFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SatisfactionPercentageTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewRequestMapper requestMapper;

    @Mock
    private ReviewResponseMapper responseMapper;

    @Mock
    private IFileStorageService fileStorageService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void testComputeSatisfactionPercentage_NoReviews() {
        // Given
        when(reviewRepository.countByVisibleTrue()).thenReturn(0L);

        // When
        double percentage = reviewService.computeSatisfactionPercentage();

        // Then
        assertEquals(0.0, percentage);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository, never()).findByVisibleTrue();
    }

    @Test
    void testComputeSatisfactionPercentage_AllFiveStars() {
        // Given
        Review review1 = new Review("PID","client1", "apt1", "John", "Great!", true, Rating.FIVE, false, "Interior");
        Review review2 = new Review("PID","client2", "apt2", "Jane", "Excellent!", true, Rating.FIVE, false, "Interior");
        Review review3 = new Review("PID","client3", "apt3", "Bob", "Perfect!", true, Rating.FIVE, false, "Interior");

        when(reviewRepository.countByVisibleTrue()).thenReturn(3L);
        when(reviewRepository.findByVisibleTrue()).thenReturn(Arrays.asList(review1, review2, review3));

        // When
        double percentage = reviewService.computeSatisfactionPercentage();

        // Then
        assertEquals(100.0, percentage, 0.01);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository).findByVisibleTrue();
    }

    @Test
    void testComputeSatisfactionPercentage_AllOneStars() {
        // Given
        Review review1 = new Review("PID","client1", "apt1", "John", "Bad", true, Rating.ONE, false, "Interior");
        Review review2 = new Review("PID","client2", "apt2", "Jane", "Terrible", true, Rating.ONE, false, "Interior");

        when(reviewRepository.countByVisibleTrue()).thenReturn(2L);
        when(reviewRepository.findByVisibleTrue()).thenReturn(Arrays.asList(review1, review2));

        // When
        double percentage = reviewService.computeSatisfactionPercentage();

        // Then
        assertEquals(20.0, percentage, 0.01);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository).findByVisibleTrue();
    }

    @Test
    void testComputeSatisfactionPercentage_MixedRatings() {
        // Given: ratings are 5, 4, 3, 2, 1 -> average = 3.0 -> 60%
        Review review1 = new Review("PID","client1", "apt1", "John", "Excellent", true, Rating.FIVE, false, "Interior");
        Review review2 = new Review("PID","client2", "apt2", "Jane", "Good", true, Rating.FOUR, false, "Interior");
        Review review3 = new Review("PID","client3", "apt3", "Bob", "Average", true, Rating.THREE, false, "Interior");
        Review review4 = new Review("PID","client4", "apt4", "Alice", "Below", true, Rating.TWO, false, "Interior");
        Review review5 = new Review("PID","client5", "apt5", "Tom", "Poor", true, Rating.ONE, false, "interior");

        when(reviewRepository.countByVisibleTrue()).thenReturn(5L);
        when(reviewRepository.findByVisibleTrue()).thenReturn(Arrays.asList(review1, review2, review3, review4, review5));

        // When
        double percentage = reviewService.computeSatisfactionPercentage();

        // Then
        // Average: (5 + 4 + 3 + 2 + 1) / 5 = 15 / 5 = 3.0
        // Percentage: (3.0 / 5.0) * 100 = 60.0
        assertEquals(60.0, percentage, 0.01);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository).findByVisibleTrue();
    }

    @Test
    void testComputeSatisfactionPercentage_FourPointFiveAverage() {
        // Given: ratings are 5, 5, 4, 4, 5 -> average = 4.6 -> 92%
        Review review1 = new Review("PID","client1", "apt1", "John", "Excellent", true, Rating.FIVE, false, "Interior");
        Review review2 = new Review("PID","client2", "apt2", "Jane", "Excellent", true, Rating.FIVE, false, "Interior");
        Review review3 = new Review("PID","client3", "apt3", "Bob", "Good", true, Rating.FOUR, false, "Interior");
        Review review4 = new Review("PID","client4", "apt4", "Alice", "Good", true, Rating.FOUR, false, "Interior");
        Review review5 = new Review("PID","client5", "apt5", "Tom", "Excellent", true, Rating.FIVE, false, "Interior");

        when(reviewRepository.countByVisibleTrue()).thenReturn(5L);
        when(reviewRepository.findByVisibleTrue()).thenReturn(Arrays.asList(review1, review2, review3, review4, review5));

        // When
        double percentage = reviewService.computeSatisfactionPercentage();

        // Then
        // Average: (5 + 5 + 4 + 4 + 5) / 5 = 23 / 5 = 4.6
        // Percentage: (4.6 / 5.0) * 100 = 92.0
        assertEquals(92.0, percentage, 0.01);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository).findByVisibleTrue();
    }

    @Test
    void testRatingEnumGetValue() {
        // Test that Rating enum getValue() returns correct numeric values
        assertEquals(1, Rating.ONE.getValue());
        assertEquals(2, Rating.TWO.getValue());
        assertEquals(3, Rating.THREE.getValue());
        assertEquals(4, Rating.FOUR.getValue());
        assertEquals(5, Rating.FIVE.getValue());
    }
}

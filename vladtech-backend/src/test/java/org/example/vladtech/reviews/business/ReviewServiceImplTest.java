package org.example.vladtech.reviews.business;

import org.example.vladtech.filestorageservice.FileStorageService;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.reviews.mapperlayer.ReviewRequestMapper;
import org.example.vladtech.reviews.mapperlayer.ReviewResponseMapper;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewRequestMapper requestMapper;

    @Mock
    private ReviewResponseMapper responseMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private FileStorageService fileStorageService;
    @Test
    void getAllVisibleReviews_returnsMappedList() {
        Review r1 = new Review("client1", "appt1", "good", true, Rating.FIVE);
        r1.setReviewId("r1");
        Review r2 = new Review("client2", "appt2", "ok", true, Rating.THREE);
        r2.setReviewId("r2");

        List<Review> repoResult = Arrays.asList(r1, r2);

        ReviewResponseModel m1 =
                new ReviewResponseModel("r1", "client1", "appt1", "good", true, Rating.FIVE, null);
        ReviewResponseModel m2 =
                new ReviewResponseModel("r2", "client2", "appt2", "ok", true, Rating.THREE, null);

        when(reviewRepository.findByVisibleTrue()).thenReturn(repoResult);
        when(responseMapper.entityListToResponseModelList(repoResult))
                .thenReturn(Arrays.asList(m1, m2));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews();

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());

        verify(reviewRepository).findByVisibleTrue();
        verify(responseMapper).entityListToResponseModelList(repoResult);
        verifyNoMoreInteractions(reviewRepository, responseMapper, requestMapper);
    }

    @Test
    void getAllReviews_returnsMappedList() {
        Review r1 = new Review("client1", "appt1", "good", true, null);
        r1.setReviewId("r1");
        Review r2 = new Review("client2", "appt2", "ok", true, null);
        r2.setReviewId("r2");

        List<Review> repoResult = Arrays.asList(r1, r2);

        ReviewResponseModel m1 = new ReviewResponseModel("r1", "client1", "appt1", "good", true, null, null);
        ReviewResponseModel m2 = new ReviewResponseModel("r2", "client2", "appt2", "ok", true, null, null);

        when(reviewRepository.findAll()).thenReturn(repoResult);
        when(responseMapper.entityListToResponseModelList(repoResult)).thenReturn(Arrays.asList(m1, m2));

        List<ReviewResponseModel> result = reviewService.getAllReviews();

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());

        verify(reviewRepository).findAll();
        verify(responseMapper).entityListToResponseModelList(repoResult);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void updateReviewVisibility_updatesVisibilityAndReturnsMappedReview() {
        String reviewId = "r1";
        boolean visible = false;

        Review existing = new Review("client1", "appt1", "good", true, null);
        existing.setReviewId(reviewId);

        Review updated = new Review("client1", "appt1", "good", visible, null);
        updated.setReviewId(reviewId);

        ReviewResponseModel responseModel = new ReviewResponseModel(reviewId, "client1", "appt1", "good", visible, null, null);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(existing)).thenReturn(updated);
        when(responseMapper.entityToResponseModel(updated)).thenReturn(responseModel);

        ReviewResponseModel result = reviewService.updateReviewVisibility(reviewId, visible);

        assertEquals(reviewId, result.getReviewId());
        assertFalse(result.getVisible());

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).save(existing);
        verify(responseMapper).entityToResponseModel(updated);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }


    @Test
    void getReviewById_throwsExceptionWhenReviewNotFound() {
        String reviewId = "r1";

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> reviewService.GetReviewById(reviewId));
        assertEquals("Review not found", exception.getMessage());

        verify(reviewRepository).findById(reviewId);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getReviewById_callsResponseMapperWithCorrectReview() {
        String reviewId = "r1";

        Review review = new Review("client1", "appt1", "Excellent service", true, null);
        review.setReviewId(reviewId);

        ReviewResponseModel expectedResponse = new ReviewResponseModel(reviewId, "client1", "appt1", "Excellent service", true, null, null);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(responseMapper.entityToResponseModel(review)).thenReturn(expectedResponse);

        ReviewResponseModel result = reviewService.GetReviewById(reviewId);

        assertEquals(expectedResponse, result);

        verify(reviewRepository).findById(reviewId);
        verify(responseMapper).entityToResponseModel(review);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

}

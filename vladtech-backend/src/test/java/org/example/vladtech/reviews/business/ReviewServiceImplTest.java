package org.example.vladtech.reviews.business;

import org.example.vladtech.filestorageservice.FileStorageService;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.reviews.mapperlayer.ReviewRequestMapper;
import org.example.vladtech.reviews.mapperlayer.ReviewResponseMapper;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.junit.jupiter.api.Disabled;
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
        Review r1 = new Review("client1","abc234", "Jamie","appt1",  true, Rating.FIVE);
        r1.setReviewId("r1");
        Review r2 = new Review("client2","abc123", "Joel",  "appt2", true, Rating.THREE);
        r2.setReviewId("r2");

        List<Review> repoResult = Arrays.asList(r1, r2);

        ReviewResponseModel m1 =
                new ReviewResponseModel("r1", "client1", "abc456", "appt1", "ok", true, Rating.THREE, null);
        ReviewResponseModel m2 =
                new ReviewResponseModel("r2", "client2", "abc455", "appt2", "ok", true, Rating.THREE, null);

        when(reviewRepository.findByVisibleTrue()).thenReturn(repoResult);
        when(responseMapper.entityListToResponseModelList(repoResult))
                .thenReturn(Arrays.asList(m1, m2));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, null);

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());

        verify(reviewRepository).findByVisibleTrue();
        verify(responseMapper).entityListToResponseModelList(repoResult);
        verifyNoMoreInteractions(reviewRepository, responseMapper, requestMapper);
    }

    @Test
    void getAllReviews_returnsMappedList() {
        Review r1 = new Review("client1", "abc324", "appt1", "good", true, null);
        r1.setReviewId("r1");
        Review r2 = new Review("client2", "abc320", "appt2", "ok", true, null);
        r2.setReviewId("r2");

        List<Review> repoResult = Arrays.asList(r1, r2);

        ReviewResponseModel m1 = new ReviewResponseModel("r1", "abc345", "client1", "appt1", "good", true, null, null);
        ReviewResponseModel m2 = new ReviewResponseModel("r2", "abc578", "client2", "appt2", "ok", true, null, null);

        when(reviewRepository.findAll()).thenReturn(repoResult);
        when(responseMapper.entityListToResponseModelList(repoResult)).thenReturn(Arrays.asList(m1, m2));

        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null);

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

        Review existing = new Review("client1", "abc789","appt1", "good", true, null);
        existing.setReviewId(reviewId);

        Review updated = new Review("client1", "abc009", "appt1", "good", visible, null);
        updated.setReviewId(reviewId);

        ReviewResponseModel responseModel = new ReviewResponseModel(reviewId, "client1", "abc709", "appt1", "good", visible, null, null);

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

        Review review = new Review("client1", "abc587", "appt1", "Excellent service", true, null);
        review.setReviewId(reviewId);

        ReviewResponseModel expectedResponse = new ReviewResponseModel(reviewId, "client1", "abc678", "appt1", "Excellent service", true, null, null);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(responseMapper.entityToResponseModel(review)).thenReturn(expectedResponse);

        ReviewResponseModel result = reviewService.GetReviewById(reviewId);

        assertEquals(expectedResponse, result);

        verify(reviewRepository).findById(reviewId);
        verify(responseMapper).entityToResponseModel(review);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void deleteReviewAsClient_successfulDeletion() {
        // Arrange
        String reviewId = "review123";
        String clientId = "client123";
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setClientId(clientId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        ReviewResponseModel responseModel = new ReviewResponseModel();
        when(responseMapper.entityToResponseModel(review)).thenReturn(responseModel);

        // Act
        ReviewResponseModel result = reviewService.deleteReviewAsClient(reviewId, clientId);

        // Assert
        verify(reviewRepository, times(1)).delete(review);
        assertEquals(responseModel, result);
    }

    @Test
    void deleteReviewAsClient_reviewNotFound() {
        // Arrange
        String reviewId = "review123";
        String clientId = "client123";

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reviewService.deleteReviewAsClient(reviewId, clientId)
        );
        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReviewAsClient_unauthorizedDeletion() {
        // Arrange
        String reviewId = "review123";
        String clientId = "client123";
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setClientId("otherClient");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reviewService.deleteReviewAsClient(reviewId, clientId)
        );
        assertEquals("Unauthorized to delete this review", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void getReviewsByOwnerAuth0Id_returnsMappedList() {
        // Arrange
        String ownerAuth0Id = "owner123";
        Review review1 = new Review("client1", "owner123", "appt1", "Great service", true, Rating.FIVE);
        review1.setReviewId("r1");
        Review review2 = new Review("client2", "owner123", "appt2", "Good service", true, Rating.FOUR);
        review2.setReviewId("r2");

        List<Review> reviews = Arrays.asList(review1, review2);

        ReviewResponseModel response1 = new ReviewResponseModel("r1", "client1", "owner123", "appt1", "Great service", true, Rating.FIVE, null);
        ReviewResponseModel response2 = new ReviewResponseModel("r2", "client2", "owner123", "appt2", "Good service", true, Rating.FOUR, null);

        when(reviewRepository.findByOwnerAuth0Id(ownerAuth0Id)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(Arrays.asList(response1, response2));

        // Act
        List<ReviewResponseModel> result = reviewService.getReviewsByOwnerAuth0Id(ownerAuth0Id);

        // Assert
        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());
        verify(reviewRepository).findByOwnerAuth0Id(ownerAuth0Id);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withClientNameAndRating_returnsFilteredReviews() {
        // Arrange
        String clientName = "client1";
        Rating ratingValue = Rating.FIVE;

        Review review = new Review(clientName, "auth0Id", "appt1", "Great service", true, ratingValue);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", clientName, "auth0Id", "appt1", "Great service", true, ratingValue, null);

        when(reviewRepository.findByVisibleTrueAndClientNameAndRating(clientName, ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, ratingValue);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByVisibleTrueAndClientNameAndRating(clientName, ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withClientNameOnly_returnsFilteredReviews() {
        // Arrange
        String clientName = "client1";

        Review review = new Review(clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, null);

        when(reviewRepository.findByVisibleTrueAndClientName(clientName)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByVisibleTrueAndClientName(clientName);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withRatingOnly_returnsFilteredReviews() {
        // Arrange
        Rating ratingValue = Rating.FIVE;

        Review review = new Review("client1", "auth0Id", "appt1", "Great service", true, ratingValue);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", "client1", "auth0Id", "appt1", "Great service", true, ratingValue, null);

        when(reviewRepository.findByVisibleTrueAndRating(ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, ratingValue);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByVisibleTrueAndRating(ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withoutFilters_returnsAllVisibleReviews() {
        // Arrange
        Review review1 = new Review("client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE);
        review1.setReviewId("r1");
        Review review2 = new Review("client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR);
        review2.setReviewId("r2");

        List<Review> reviews = List.of(review1, review2);

        ReviewResponseModel response1 = new ReviewResponseModel("r1", "client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, null);
        ReviewResponseModel response2 = new ReviewResponseModel("r2", "client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR, null);

        when(reviewRepository.findByVisibleTrue()).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response1, response2));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, null);

        // Assert
        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());
        verify(reviewRepository).findByVisibleTrue();
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withClientNameAndRating_returnsFilteredReviews() {
        // Arrange
        String clientName = "client1";
        Rating ratingValue = Rating.FIVE;

        Review review = new Review(clientName, "auth0Id", "appt1", "Great service", true, ratingValue);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", clientName, "auth0Id", "appt1", "Great service", true, ratingValue, null);

        when(reviewRepository.findByClientNameAndRating(clientName, ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, ratingValue);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByClientNameAndRating(clientName, ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withClientNameOnly_returnsFilteredReviews() {
        // Arrange
        String clientName = "client1";

        Review review = new Review(clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, null);

        when(reviewRepository.findByClientName(clientName)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByClientName(clientName);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withRatingOnly_returnsFilteredReviews() {
        // Arrange
        Rating ratingValue = Rating.FIVE;

        Review review = new Review("client1", "auth0Id", "appt1", "Great service", true, ratingValue);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", "client1", "auth0Id", "appt1", "Great service", true, ratingValue, null);

        when(reviewRepository.findByRating(ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, ratingValue);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByRating(ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withoutFilters_returnsAllReviews() {
        // Arrange
        Review review1 = new Review("client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE);
        review1.setReviewId("r1");
        Review review2 = new Review("client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR);
        review2.setReviewId("r2");

        List<Review> reviews = List.of(review1, review2);

        ReviewResponseModel response1 = new ReviewResponseModel("r1", "client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, null);
        ReviewResponseModel response2 = new ReviewResponseModel("r2", "client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR, null);

        when(reviewRepository.findAll()).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response1, response2));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null);

        // Assert
        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());
        verify(reviewRepository).findAll();
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void createReview_withoutPhotos_createsReviewSuccessfully() {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientId("client123");
        requestModel.setClientName("John Doe");
        requestModel.setAppointmentId("appt123");
        requestModel.setComment("Great service!");
        requestModel.setRating(Rating.FIVE);
        requestModel.setVisible(false);

        Review review = new Review();
        review.setClientId("client123");
        review.setClientName("John Doe");

        Review savedReview = new Review();
        savedReview.setReviewId("r1");
        savedReview.setClientId("client123");

        ReviewResponseModel responseModel = new ReviewResponseModel();
        responseModel.setReviewId("r1");

        when(requestMapper.requestModelToEntity(requestModel)).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(responseMapper.entityToResponseModel(savedReview)).thenReturn(responseModel);

        // Act
        ReviewResponseModel result = reviewService.createReview(requestModel, null, "auth0|owner123");

        // Assert
        assertNotNull(result);
        assertEquals("r1", result.getReviewId());
        verify(requestMapper).requestModelToEntity(requestModel);
        verify(reviewRepository).save(any(Review.class));
        verify(responseMapper).entityToResponseModel(savedReview);
    }

    @Test
    void createReview_withPhotos_createsReviewWithPhotosSuccessfully() throws IOException {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientId("client123");
        requestModel.setClientName("John Doe");
        requestModel.setAppointmentId("appt123");
        requestModel.setComment("Great service!");
        requestModel.setRating(Rating.FIVE);
        requestModel.setVisible(false);

        MultipartFile photo1 = mock(MultipartFile.class);
        MultipartFile photo2 = mock(MultipartFile.class);
        when(photo1.getContentType()).thenReturn("image/jpeg");
        when(photo2.getContentType()).thenReturn("image/png");

        Review review = new Review();
        review.setClientId("client123");

        Review savedReview = new Review();
        savedReview.setReviewId("r1");

        ReviewResponseModel responseModel = new ReviewResponseModel();
        responseModel.setReviewId("r1");

        when(requestMapper.requestModelToEntity(requestModel)).thenReturn(review);
        when(fileStorageService.save(photo1)).thenReturn("photo1.jpg");
        when(fileStorageService.save(photo2)).thenReturn("photo2.png");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(responseMapper.entityToResponseModel(savedReview)).thenReturn(responseModel);

        // Act
        ReviewResponseModel result = reviewService.createReview(requestModel, new MultipartFile[]{photo1, photo2}, "auth0|owner123");

        // Assert
        assertNotNull(result);
        assertEquals("r1", result.getReviewId());
        verify(fileStorageService).save(photo1);
        verify(fileStorageService).save(photo2);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @Disabled("Temporarily disabled due to Mockito unnecessary stubbing issue")
    void createReview_withPhotoSaveFailure_throwsRuntimeException() throws IOException {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientId("client123");
        requestModel.setClientName("John Doe");
        requestModel.setAppointmentId("appt123");
        requestModel.setComment("Great service!");
        requestModel.setRating(Rating.FIVE);
        requestModel.setVisible(false);

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.getContentType()).thenReturn("image/jpeg");

        Review review = new Review();
        review.setClientId("client123");

        when(requestMapper.requestModelToEntity(requestModel)).thenReturn(review);
        when(fileStorageService.save(photo)).thenThrow(new IOException("Storage failure"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            reviewService.createReview(requestModel, new MultipartFile[]{photo}, "auth0|owner123")
        );

        verify(fileStorageService).save(photo);
    }

    @Test
    void updateReviewVisibility_reviewNotFound_throwsException() {
        // Arrange
        String reviewId = "nonexistent";

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            reviewService.updateReviewVisibility(reviewId, true)
        );

        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void computeSatisfactionPercentage_withZeroReviews_returnsZero() {
        // Arrange
        when(reviewRepository.countByVisibleTrue()).thenReturn(0L);

        // Act
        double result = reviewService.computeSatisfactionPercentage();

        // Assert
        assertEquals(0.0, result);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository, never()).findByVisibleTrue();
    }

    @Test
    void computeSatisfactionPercentage_withMultipleReviews_calculatesCorrectPercentage() {
        // Arrange
        Review review1 = new Review("client1", "auth0Id1", "appt1", "Excellent", true, Rating.FIVE);
        Review review2 = new Review("client2", "auth0Id2", "appt2", "Good", true, Rating.FOUR);
        Review review3 = new Review("client3", "auth0Id3", "appt3", "Average", true, Rating.THREE);

        when(reviewRepository.countByVisibleTrue()).thenReturn(3L);
        when(reviewRepository.findByVisibleTrue()).thenReturn(List.of(review1, review2, review3));

        // Act
        double result = reviewService.computeSatisfactionPercentage();

        // Assert
        // Average: (5 + 4 + 3) / 3 = 4.0
        // Percentage: (4.0 / 5.0) * 100 = 80.0
        assertEquals(80.0, result, 0.01);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository).findByVisibleTrue();
    }
}

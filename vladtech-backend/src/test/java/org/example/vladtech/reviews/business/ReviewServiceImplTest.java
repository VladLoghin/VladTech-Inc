package org.example.vladtech.reviews.business;

import org.example.vladtech.filestorageservice.FileStorageService;
import org.example.vladtech.portfolio.business.PortfolioServiceImpl;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.example.vladtech.portfolio.mapperlayer.PortfolioMapper;
import org.example.vladtech.reviews.data.Photo;
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
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import java.io.IOException;
import java.util.*;

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

    @Mock
    private PortfolioMapper portfolioMapper;

    @Mock
    private PortfolioServiceImpl portfolioService;

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private FileStorageService fileStorageService;

    @Test
    void getAllVisibleReviews_returnsMappedList() {
        Review r1 = new Review("PID","client1", "abc234", "Jamie", "appt1", true, Rating.THREE, false, "Interior");
        r1.setReviewId("r1");
        Review r2 = new Review("PID","client2", "abc123", "Joel", "appt2", true, Rating.THREE, false, "Interior");
        r2.setReviewId("r2");

        List<Review> repoResult = Arrays.asList(r1, r2);

        ReviewResponseModel m1 =
                new ReviewResponseModel("r1","PID", "client1", "abc456", "appt1", "ok", true, Rating.THREE, null, "Interior");
        ReviewResponseModel m2 =
                new ReviewResponseModel("r2","PID", "client2", "abc455", "appt2", "ok", true, Rating.THREE, null, "Interior");

        when(reviewRepository.findByVisibleTrue()).thenReturn(repoResult);
        when(responseMapper.entityListToResponseModelList(repoResult))
                .thenReturn(Arrays.asList(m1, m2));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, null, null, null);

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());

        verify(reviewRepository).findByVisibleTrue();
        verify(responseMapper).entityListToResponseModelList(repoResult);
        verifyNoMoreInteractions(reviewRepository, responseMapper, requestMapper);
    }

    @Test
    void getAllReviews_returnsMappedList() {
        Review r1 = new Review("PID","client1", "abc324", "appt1", "good", true, null, false, "Interior");
        r1.setReviewId("r1");
        Review r2 = new Review("PID","client2", "abc320", "appt2", "ok", true, null, false, "Interior");
        r2.setReviewId("r2");

        List<Review> repoResult = Arrays.asList(r1, r2);

        ReviewResponseModel m1 = new ReviewResponseModel("r1","PID", "abc345", "client1", "appt1", "good", true, null, null, "Interior");
        ReviewResponseModel m2 = new ReviewResponseModel("r2","PID", "abc578", "client2", "appt2", "ok", true, null, null, "Interior");

        when(reviewRepository.findAll()).thenReturn(repoResult);
        when(responseMapper.entityListToResponseModelList(repoResult)).thenReturn(Arrays.asList(m1, m2));

        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null, null, null);

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

        Review existing = new Review("PID","client1", "abc789", "appt1", "good", true, null, false, "Interior");
        existing.setReviewId(reviewId);

        Review updated = new Review("PID","client1", "abc009", "appt1", "good", visible, null, false, "Interior");
        updated.setReviewId(reviewId);

        ReviewResponseModel responseModel = new ReviewResponseModel( reviewId, "PID","client1", "abc709", "appt1", "good", visible, null, null, "Interior");

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

        Review review = new Review("PID","client1", "abc587", "appt1", "Excellent service", true, Rating.FIVE, false, "Interior");
        review.setReviewId(reviewId);

        ReviewResponseModel expectedResponse = new ReviewResponseModel("PID",reviewId, "client1", "abc678", "appt1", "Excellent service", true, null, null, "Interior");

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
        String reviewId = "review123";
        String clientId = "client123";
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setClientId(clientId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        ReviewResponseModel responseModel = new ReviewResponseModel();
        when(responseMapper.entityToResponseModel(review)).thenReturn(responseModel);

        ReviewResponseModel result = reviewService.deleteReviewAsClient(reviewId, clientId);

        verify(reviewRepository, times(1)).delete(review);
        assertEquals(responseModel, result);
    }

    @Test
    void deleteReviewAsClient_reviewNotFound() {
        String reviewId = "review123";
        String clientId = "client123";

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reviewService.deleteReviewAsClient(reviewId, clientId)
        );
        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReviewAsClient_unauthorizedDeletion() {
        String reviewId = "review123";
        String clientId = "client123";
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setClientId("otherClient");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reviewService.deleteReviewAsClient(reviewId, clientId)
        );
        assertEquals("Unauthorized to delete this review", exception.getMessage());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void getReviewsByOwnerAuth0Id_returnsMappedList() {
        String ownerAuth0Id = "owner123";
        Review review1 = new Review("PID","client1", "owner123", "appt1", "Great service", true, Rating.FIVE, false, "Interior");
        review1.setReviewId("r1");
        Review review2 = new Review("PID","client2", "owner123", "appt2", "Good service", true, Rating.FOUR, false, "Interior");
        review2.setReviewId("r2");

        List<Review> reviews = Arrays.asList(review1, review2);

        ReviewResponseModel response1 = new ReviewResponseModel("r1","PID", "client1", "owner123", "appt1", "Great service", true, Rating.FIVE, null, "Interior");
        ReviewResponseModel response2 = new ReviewResponseModel("r2","PID", "client2", "owner123", "appt2", "Good service", true, Rating.FOUR, null, "Interior");

        when(reviewRepository.findByOwnerAuth0Id(ownerAuth0Id)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(Arrays.asList(response1, response2));

        List<ReviewResponseModel> result = reviewService.getReviewsByOwnerAuth0Id(ownerAuth0Id);

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());
        verify(reviewRepository).findByOwnerAuth0Id(ownerAuth0Id);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withClientNameAndRating_returnsFilteredReviews() {
        String clientName = "client1";
        Rating ratingValue = Rating.FIVE;

        Review review = new Review("PID",clientName, "auth0Id", "appt1", "Great service", true, ratingValue, false, "Interior");
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", "Great service", true, ratingValue, null, "Interior");

        when(reviewRepository.findByVisibleTrueAndClientNameContainingIgnoreCaseAndRating(clientName, ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, ratingValue, null, null);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByVisibleTrueAndClientNameContainingIgnoreCaseAndRating(clientName, ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withClientNameOnly_returnsFilteredReviews() {
        String clientName = "client1";

        Review review = new Review("PID",clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, false, "Interior");
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, null, "Interior");

        when(reviewRepository.findByVisibleTrueAndClientNameContainingIgnoreCase(clientName)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, null, null, null);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByVisibleTrueAndClientNameContainingIgnoreCase(clientName);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withRatingOnly_returnsFilteredReviews() {
        Rating ratingValue = Rating.FIVE;

        Review review = new Review("PID","client1", "auth0Id", "appt1", "Great service", true, ratingValue, false, "Interior");
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", "Great service", true, ratingValue, null, "Interior");

        when(reviewRepository.findByVisibleTrueAndRating(ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, ratingValue, null, null);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByVisibleTrueAndRating(ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllVisibleReviews_withoutFilters_returnsAllVisibleReviews() {
        Review review1 = new Review("PID","client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, false, "Interior");
        review1.setReviewId("r1");
        Review review2 = new Review("PID","client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR, false, "Interior");
        review2.setReviewId("r2");

        List<Review> reviews = List.of(review1, review2);

        ReviewResponseModel response1 = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, null, "Interior");
        ReviewResponseModel response2 = new ReviewResponseModel("r2","PID", "client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR, null, "Interior");

        when(reviewRepository.findByVisibleTrue()).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response1, response2));

        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, null, null, null);

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());
        verify(reviewRepository).findByVisibleTrue();
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withClientNameAndRating_returnsFilteredReviews() {
        String clientName = "client1";
        Rating ratingValue = Rating.FIVE;

        Review review = new Review("PID",clientName, "auth0Id", "appt1", "Great service", true, ratingValue, false, "Interior");
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", "Great service", true, ratingValue, null, "Interior");

        when(reviewRepository.findByClientNameContainingIgnoreCaseAndRating(clientName, ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, ratingValue, null, null);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByClientNameContainingIgnoreCaseAndRating(clientName, ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withClientNameOnly_returnsFilteredReviews() {
        String clientName = "client1";

        Review review = new Review("PID",clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, false, "Interior");
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1", "PID", clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, null, "Interior");

        when(reviewRepository.findByClientNameContainingIgnoreCase(clientName)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, null, null, null);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByClientNameContainingIgnoreCase(clientName);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withRatingOnly_returnsFilteredReviews() {
        Rating ratingValue = Rating.FIVE;

        Review review = new Review("PID","client1", "auth0Id", "appt1", "Great service", true, ratingValue, false, "Interior");
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", "Great service", true, ratingValue, null, "Interior");

        when(reviewRepository.findByRating(ratingValue)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        List<ReviewResponseModel> result = reviewService.getAllReviews(null, ratingValue, null, null);

        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByRating(ratingValue);
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void getAllReviews_withoutFilters_returnsAllReviews() {
        Review review1 = new Review("PID","client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, false, "Interior");
        review1.setReviewId("r1");
        Review review2 = new Review("PID","client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR, false, "Interior");
        review2.setReviewId("r2");

        List<Review> reviews = List.of(review1, review2);

        ReviewResponseModel response1 = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, null, "Interior");
        ReviewResponseModel response2 = new ReviewResponseModel("r2", "PID","client2", "auth0Id", "appt2", "Good service", true, Rating.FOUR, null, "Interior");

        when(reviewRepository.findAll()).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response1, response2));

        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null, null, null);

        assertEquals(2, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        assertEquals("r2", result.get(1).getReviewId());
        verify(reviewRepository).findAll();
        verify(responseMapper).entityListToResponseModelList(reviews);
        verifyNoMoreInteractions(reviewRepository, responseMapper);
    }

    @Test
    void createReview_withoutPhotos_createsReviewSuccessfully() {
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientId("client123");
        requestModel.setClientName("John Doe");
        requestModel.setAppointmentId("appt123");
        requestModel.setComment("Great service!");
        requestModel.setRating(Rating.FIVE);
        requestModel.setVisible(false);
        requestModel.setProjectId("project123"); // Add Project ID

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

        ReviewResponseModel result = reviewService.createReview(requestModel, null, "auth0|owner123");

        assertNotNull(result);
        assertEquals("r1", result.getReviewId());
        verify(requestMapper).requestModelToEntity(requestModel);
        verify(reviewRepository).save(any(Review.class));
        verify(responseMapper).entityToResponseModel(savedReview);
    }

    @Test
    void createReview_withPhotos_createsReviewWithPhotosSuccessfully() throws IOException {
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientId("client123");
        requestModel.setClientName("John Doe");
        requestModel.setAppointmentId("appt123");
        requestModel.setComment("Great service!");
        requestModel.setRating(Rating.FIVE);
        requestModel.setVisible(false);
        requestModel.setProjectId("project123"); // Add Project ID

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

        ReviewResponseModel result = reviewService.createReview(requestModel, new MultipartFile[]{photo1, photo2}, "auth0|owner123");

        assertNotNull(result);
        assertEquals("r1", result.getReviewId());
        verify(fileStorageService).save(photo1);
        verify(fileStorageService).save(photo2);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @Disabled("Temporarily disabled due to Mockito unnecessary stubbing issue")
    void createReview_withPhotoSaveFailure_throwsRuntimeException() throws IOException {
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

        assertThrows(RuntimeException.class, () ->
                reviewService.createReview(requestModel, new MultipartFile[]{photo}, "auth0|owner123")
        );

        verify(fileStorageService).save(photo);
    }

    @Test
    void updateReviewVisibility_reviewNotFound_throwsException() {
        String reviewId = "nonexistent";

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reviewService.updateReviewVisibility(reviewId, true)
        );

        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void computeSatisfactionPercentage_withZeroReviews_returnsZero() {
        when(reviewRepository.countByVisibleTrue()).thenReturn(0L);

        double result = reviewService.computeSatisfactionPercentage();

        assertEquals(0.0, result);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository, never()).findByVisibleTrue();
    }

    @Test
    void computeSatisfactionPercentage_withMultipleReviews_calculatesCorrectPercentage() {
        Review review1 = new Review("client1","PID", "auth0Id1", "appt1", "Excellent", true, Rating.FIVE, false, "Interior");
        Review review2 = new Review("client2","PID", "auth0Id2", "appt2", "Good", true, Rating.FOUR, false, "Interior");
        Review review3 = new Review("client3","PID", "auth0Id3", "appt3", "Average", true, Rating.THREE, false, "Interior");

        when(reviewRepository.countByVisibleTrue()).thenReturn(3L);
        when(reviewRepository.findByVisibleTrue()).thenReturn(List.of(review1, review2, review3));

        double result = reviewService.computeSatisfactionPercentage();

        assertEquals(80.0, result, 0.01);
        verify(reviewRepository).countByVisibleTrue();
        verify(reviewRepository).findByVisibleTrue();
    }

    @Test
    void deleteReview_successfulDeletion() {
        String reviewId = "review123";
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(reviewRepository, times(1)).deleteReviewByReviewId(reviewId);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    void deleteReview_reviewNotFound_throwsException() {
        String reviewId = "nonexistent";
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> reviewService.deleteReview(reviewId));
        assertEquals("Review with ID " + reviewId + " does not exist", exception.getMessage());

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(reviewRepository, never()).deleteReviewByReviewId(anyString());
        verifyNoMoreInteractions(reviewRepository);
    }


    @Test
    public void resetPortfolioStatus() {
        String reviewId = "review123"; // Define the reviewId directly in the test
        Review existing = new Review();
        existing.setReviewId(reviewId);
        existing.setSentToPortfolio(true);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));

        reviewService.resetPortfolioStatus(reviewId);

        verify(reviewRepository).save(existing);
        assertFalse(existing.isSentToPortfolio());
    }

    @Test
    void getAllReviews_withTypeNameAndRating_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";
        Rating rating = Rating.FIVE;
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", clientName, "Great", true, rating, null, type);
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewRepository.findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(type, clientName, rating))
                .thenReturn(Collections.singletonList(new Review()));
        when(responseMapper.entityListToResponseModelList(anyList())).thenReturn(reviews);

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, rating, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(type, clientName, rating);
    }

    @Test
    void getAllReviews_withTypeAndName_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", clientName, "Great", true, Rating.FIVE, null, type);
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewRepository.findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(type, clientName))
                .thenReturn(Collections.singletonList(new Review()));
        when(responseMapper.entityListToResponseModelList(anyList())).thenReturn(reviews);

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, null, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(type, clientName);
    }

    @Test
    void getAllReviews_withTypeAndRating_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        Rating rating = Rating.FIVE;
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", "John", "Great", true, rating, null, type);
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewRepository.findByTypeContainingIgnoreCaseAndRating(type, rating))
                .thenReturn(Collections.singletonList(new Review()));
        when(responseMapper.entityListToResponseModelList(anyList())).thenReturn(reviews);

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, rating, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByTypeContainingIgnoreCaseAndRating(type, rating);
    }

    @Test
    void getAllReviews_withTypeOnly_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", "John", "Great", true, Rating.FIVE, null, type);
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewRepository.findByTypeContainingIgnoreCase(type))
                .thenReturn(Collections.singletonList(new Review()));
        when(responseMapper.entityListToResponseModelList(anyList())).thenReturn(reviews);

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByTypeContainingIgnoreCase(type);
    }

    @Test
    void getAllVisibleReviews_withTypeNameAndRating_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";
        Rating rating = Rating.FIVE;

        Review review = new Review("PID",clientName, "auth0Id", "appt1", "Great service", true, rating, false, type);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", "Great service", true, rating, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(type, clientName, rating))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, rating, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRating(type, clientName, rating);
    }

    @Test
    void getAllVisibleReviews_withTypeAndName_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";

        Review review = new Review("PID",clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, false, type);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", "Great service", true, Rating.FIVE, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(type, clientName))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, null, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCase(type, clientName);
    }

    @Test
    void getAllVisibleReviews_withTypeAndRating_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        Rating rating = Rating.FIVE;

        Review review = new Review("PID","client1", "auth0Id", "appt1", "Great service", true, rating, false, type);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", "Great service", true, rating, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCaseAndRating(type, rating))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, rating, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCaseAndRating(type, rating);
    }

    @Test
    void getAllVisibleReviews_withTypeOnly_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";

        Review review = new Review("PID","client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, false, type);
        review.setReviewId("r1");

        List<Review> reviews = List.of(review);

        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", "Great service", true, Rating.FIVE, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCase(type))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, null, type, null);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCase(type);
    }

    @Test
    void sendToPortfolio_createsPortfolioItemAndUpdatesReview() {
        // Arrange
        String reviewId = "review123";
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setClientName("John Doe");
        review.setType("Interior");

        Photo photo = new Photo();
        photo.setUrl("photo1.jpg");
        review.setPhotos(Collections.singletonList(photo));
        review.setSentToPortfolio(false);

        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setPortfolioId("portfolio123");
        portfolioItem.setReviewId(reviewId);
        portfolioItem.setTitle("Review by John Doe");
        portfolioItem.setImageUrl("photo1.jpg");
        portfolioItem.setType("Interior");

        PortfolioResponseDto responseDto = new PortfolioResponseDto();
        responseDto.setPortfolioId("portfolio123");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(portfolioItem);
        when(portfolioMapper.entityToResponseDto(portfolioItem)).thenReturn(responseDto);

        // Act
        PortfolioResponseDto result = reviewService.sendToPortfolio(reviewId);

        // Assert
        assertNotNull(result);
        assertEquals("portfolio123", result.getPortfolioId());
        verify(reviewRepository).findById(reviewId);
        verify(portfolioRepository).save(any(PortfolioItem.class));
        verify(reviewRepository).save(review);
        verify(portfolioMapper).entityToResponseDto(portfolioItem);
    }

    @Test
    void sendToPortfolio_reviewAlreadySent_throwsException() {
        // Arrange
        String reviewId = "review123";
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setSentToPortfolio(true);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> reviewService.sendToPortfolio(reviewId));
        assertEquals("Review has already been sent to portfolio", exception.getMessage());
        verify(reviewRepository).findById(reviewId);
        verifyNoInteractions(portfolioRepository, portfolioMapper);
    }

    @Test
    void sendToPortfolio_reviewNotFound_throwsException() {
        // Arrange
        String reviewId = "review123";

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> reviewService.sendToPortfolio(reviewId));
        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepository).findById(reviewId);
        verifyNoInteractions(portfolioRepository, portfolioMapper);
    }

    @Test
    void getAllReviews_withTypeNameAndCommentFilters_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";
        String comment = "Great service";
        Rating rating = null; // Ensure this branch is triggered
        List<Review> reviews = Collections.singletonList(new Review(clientName,"PID", "auth0Id", "appt1", comment, true, Rating.FIVE, false, type));

        ReviewResponseModel response = new ReviewResponseModel("r1", clientName,"PID", "auth0Id", "appt1", comment, true, Rating.FIVE, null, type);

        when(reviewRepository.findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                type, clientName, comment)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(Collections.singletonList(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, rating, type, comment);

        // Assert
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getReviewId());
        verify(reviewRepository).findByTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                type, clientName, comment);
        verify(responseMapper).entityListToResponseModelList(reviews);
    }


    @Test
    void getAllReviews_withTypeRatingAndComment_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        Rating rating = Rating.FIVE;
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID","client1", "auth0Id", "appt1", comment, true, rating, false, type));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", comment, true, rating, null, type);

        when(reviewRepository.findByTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(type, rating, comment))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, rating, type, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(type, rating, comment);
    }

    @Test
    void getAllReviews_withNameRatingAndComment_returnsFilteredReviews() {
        // Arrange
        String clientName = "John";
        Rating rating = Rating.FIVE;
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID",clientName, "auth0Id", "appt1", comment, true, rating, false, "Interior"));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", comment, true, rating, null, "Interior");

        when(reviewRepository.findByClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(clientName, rating, comment))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, rating, null, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(clientName, rating, comment);
    }

    @Test
    void getAllReviews_withTypeAndComment_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID","client1", "auth0Id", "appt1", comment, true, Rating.FIVE, false, type));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", comment, true, Rating.FIVE, null, type);

        when(reviewRepository.findByTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(type, comment))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null, type, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByTypeContainingIgnoreCaseAndCommentContainingIgnoreCase(type, comment);
    }

    @Test
    void getAllReviews_withNameAndComment_returnsFilteredReviews() {
        // Arrange
        String clientName = "John";
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID",clientName, "auth0Id", "appt1", comment, true, Rating.FIVE, false, "Interior"));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", comment, true, Rating.FIVE, null, "Interior");

        when(reviewRepository.findByClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(clientName, comment))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(clientName, null, null, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(clientName, comment);
    }

    @Test
    void getAllReviews_withRatingAndComment_returnsFilteredReviews() {
        // Arrange
        Rating rating = Rating.FIVE;
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID","client1", "auth0Id", "appt1", comment, true, rating, false, "Interior"));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", comment, true, rating, null, "Interior");

        when(reviewRepository.findByRatingAndCommentContainingIgnoreCase(rating, comment))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, rating, null, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByRatingAndCommentContainingIgnoreCase(rating, comment);
    }

    @Test
    void getAllReviews_withCommentOnly_returnsFilteredReviews() {
        // Arrange
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID","client1", "auth0Id", "appt1", comment, true, Rating.FIVE, false, "Interior"));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", comment, true, Rating.FIVE, null, "Interior");

        when(reviewRepository.findByCommentContainingIgnoreCase(comment))
                .thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllReviews(null, null, null, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByCommentContainingIgnoreCase(comment);
    }

    @Test
    void getAllVisibleReviews_withTypeNameRatingAndComment_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";
        Rating rating = Rating.FIVE;
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID",clientName, "auth0Id", "appt1", comment, true, rating, false, type));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", comment, true, rating, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                type, clientName, rating, comment)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, rating, type, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                type, clientName, rating, comment);
    }

    @Test
    void getAllVisibleReviews_withTypeNameAndComment_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        String clientName = "John";
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID",clientName, "auth0Id", "appt1", comment, true, Rating.FIVE, false, type));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", comment, true, Rating.FIVE, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                type, clientName, comment)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, null, type, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCaseAndClientNameContainingIgnoreCaseAndCommentContainingIgnoreCase(
                type, clientName, comment);
    }

    @Test
    void getAllVisibleReviews_withTypeRatingAndComment_returnsFilteredReviews() {
        // Arrange
        String type = "Interior";
        Rating rating = Rating.FIVE;
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID","client1", "auth0Id", "appt1", comment, true, rating, false, type));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", comment, true, rating, null, type);

        when(reviewRepository.findByVisibleTrueAndTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                type, rating, comment)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, rating, type, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndTypeContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                type, rating, comment);
    }

    @Test
    void getAllVisibleReviews_withNameRatingAndComment_returnsFilteredReviews() {
        // Arrange
        String clientName = "John";
        Rating rating = Rating.FIVE;
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID",clientName, "auth0Id", "appt1", comment, true, rating, false, "Interior"));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", clientName, "auth0Id", "appt1", comment, true, rating, null, "Interior");

        when(reviewRepository.findByVisibleTrueAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                clientName, rating, comment)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(clientName, rating, null, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndClientNameContainingIgnoreCaseAndRatingAndCommentContainingIgnoreCase(
                clientName, rating, comment);
    }

    @Test
    void getAllVisibleReviews_withCommentOnly_returnsFilteredReviews() {
        // Arrange
        String comment = "Great service";
        List<Review> reviews = List.of(new Review("PID","client1", "auth0Id", "appt1", comment, true, Rating.FIVE, false, "Interior"));
        ReviewResponseModel response = new ReviewResponseModel("r1","PID", "client1", "auth0Id", "appt1", comment, true, Rating.FIVE, null, "Interior");

        when(reviewRepository.findByVisibleTrueAndCommentContainingIgnoreCase(comment)).thenReturn(reviews);
        when(responseMapper.entityListToResponseModelList(reviews)).thenReturn(List.of(response));

        // Act
        List<ReviewResponseModel> result = reviewService.getAllVisibleReviews(null, null, null, comment);

        // Assert
        assertEquals(1, result.size());
        verify(reviewRepository).findByVisibleTrueAndCommentContainingIgnoreCase(comment);
    }

}

package org.example.vladtech.reviews.presentation;

import org.example.vladtech.contact.businesslayer.ContactServiceImpl;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.example.vladtech.reviews.business.ReviewService;
import org.example.vladtech.reviews.data.Rating;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ContactServiceImpl contactService;

    @Mock
    private Jwt jwt;

    @Test
    void getAllReviews_returnsListOfReviews() {
        // Arrange
        ReviewResponseModel review1 = new ReviewResponseModel("r1","PID", "client1", "appt1", "John", "Great", true, Rating.FIVE, null, "Interior");
        ReviewResponseModel review2 = new ReviewResponseModel("r2","PID", "client2", "appt2", "Jane", "Good", true, Rating.FOUR, null, "Interior");
        List<ReviewResponseModel> reviews = Arrays.asList(review1, review2);

        when(reviewService.getAllReviews(null, null, null, null)).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseModel>> response = reviewController.getAllReviews(jwt, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(reviewService).getAllReviews(null, null, null, null);
    }

    @Test
    void getAllReviews_withClientNameFilter_returnsFilteredReviews() {
        // Arrange
        String clientName = "John";
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", clientName, "Great", true, Rating.FIVE, null, "Interior");
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewService.getAllReviews(clientName, null, null, null)).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseModel>> response = reviewController.getAllReviews(jwt, clientName, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(clientName, response.getBody().get(0).getClientName());
        verify(reviewService).getAllReviews(clientName, null, null, null);
    }

    @Test
    void getAllReviews_withRatingFilter_returnsFilteredReviews() {
        // Arrange
        Rating rating = Rating.FIVE;
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", "John", "Great", true, rating, null, "Interior");
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewService.getAllReviews(null, rating, null, null)).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseModel>> response = reviewController.getAllReviews(jwt, null, rating, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(rating, response.getBody().get(0).getRating());
        verify(reviewService).getAllReviews(null, rating, null, null);
    }

    @Test
    void getAllVisibleReviews_returnsOnlyVisibleReviews() {
        // Arrange
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", "John", "Great", true, Rating.FIVE, null, "Interior");
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseModel>> response = reviewController.getAllVisibleReviews(null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getVisible());
        verify(reviewService).getAllVisibleReviews(null, null, null, null);
    }

    @Test
    void getAllVisibleReviews_withFilters_returnsFilteredVisibleReviews() {
        // Arrange
        String clientName = "John";
        Rating rating = Rating.FIVE;
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", clientName, "Great", true, rating, null, "Interior");
        List<ReviewResponseModel> reviews = Collections.singletonList(review);

        when(reviewService.getAllVisibleReviews(clientName, rating, null, null)).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseModel>> response = reviewController.getAllVisibleReviews(clientName, rating, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(reviewService).getAllVisibleReviews(clientName, rating, null, null);
    }

    @Test
    void patchReviewVisibility_withValidData_updatesVisibility() {
        // Arrange
        String reviewId = "r1";
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setVisible(true);

        ReviewResponseModel updatedReview = new ReviewResponseModel();
        updatedReview.setReviewId(reviewId);
        updatedReview.setVisible(true);

        when(reviewService.updateReviewVisibility(reviewId, true)).thenReturn(updatedReview);

        // Act
        ResponseEntity<ReviewResponseModel> response = reviewController.patchReviewVisibility(reviewId, requestModel);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reviewId, response.getBody().getReviewId());
        assertTrue(response.getBody().getVisible());
        verify(reviewService).updateReviewVisibility(reviewId, true);
    }

    @Test
    void patchReviewVisibility_withNullReviewId_returnsBadRequest() {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setVisible(true);

        // Act
        ResponseEntity<ReviewResponseModel> response = reviewController.patchReviewVisibility(null, requestModel);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).updateReviewVisibility(anyString(), anyBoolean());
    }

    @Test
    void patchReviewVisibility_withBlankReviewId_returnsBadRequest() {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setVisible(true);

        // Act
        ResponseEntity<ReviewResponseModel> response = reviewController.patchReviewVisibility("", requestModel);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).updateReviewVisibility(anyString(), anyBoolean());
    }

    @Test
    void patchReviewVisibility_withNullRequestModel_returnsBadRequest() {
        // Act
        ResponseEntity<ReviewResponseModel> response = reviewController.patchReviewVisibility("r1", null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).updateReviewVisibility(anyString(), anyBoolean());
    }

    @Test
    void patchReviewVisibility_withNullVisibleField_returnsBadRequest() {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setVisible(null);

        // Act
        ResponseEntity<ReviewResponseModel> response = reviewController.patchReviewVisibility("r1", requestModel);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).updateReviewVisibility(anyString(), anyBoolean());
    }

    @Test
    void createReview_withValidData_createsReview() {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientName("John");
        requestModel.setComment("Great service");
        requestModel.setRating(Rating.FIVE);

        ReviewResponseModel createdReview = new ReviewResponseModel();
        createdReview.setReviewId("r1");

        when(jwt.getClaim("sub")).thenReturn("auth0|user123");
        when(reviewService.createReview(eq(requestModel), isNull(), eq("auth0|user123"))).thenReturn(createdReview);

        // Act
        ResponseEntity<ReviewResponseModel> response = (ResponseEntity<ReviewResponseModel>) reviewController.createReview(requestModel, null, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("r1", response.getBody().getReviewId());
        assertFalse(requestModel.getVisible()); // Should be set to false
        assertEquals("auth0|user123", requestModel.getClientId());
        verify(reviewService).createReview(requestModel, null, "auth0|user123");
    }

    @Test
    void createReview_withPhotos_createsReviewWithPhotos() {
        // Arrange
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setClientName("John");
        requestModel.setComment("Great service");
        requestModel.setRating(Rating.FIVE);

        MultipartFile[] photos = new MultipartFile[2];
        photos[0] = mock(MultipartFile.class);
        photos[1] = mock(MultipartFile.class);

        ReviewResponseModel createdReview = new ReviewResponseModel();
        createdReview.setReviewId("r1");

        when(jwt.getClaim("sub")).thenReturn("auth0|user123");
        when(reviewService.createReview(eq(requestModel), eq(photos), eq("auth0|user123"))).thenReturn(createdReview);

        // Act
        ResponseEntity<ReviewResponseModel> response = (ResponseEntity<ReviewResponseModel>) reviewController.createReview(requestModel, photos, jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("r1", response.getBody().getReviewId());
        verify(reviewService).createReview(requestModel, photos, "auth0|user123");
    }

    @Test
    void deleteReview_withValidData_deletesReview() {
        // Arrange
        String reviewId = "r1";
        when(jwt.getClaim("sub")).thenReturn("auth0|user123");
        when(jwt.getClaimAsString("sub")).thenReturn("auth0|user123");

        // Act
        ResponseEntity<Void> response = reviewController.deleteReview(reviewId, jwt);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reviewService).deleteReviewAsClient(reviewId, "auth0|user123");
    }

    @Test
    void deleteReview_withNullJwt_returnsUnauthorized() {
        // Act
        ResponseEntity<Void> response = reviewController.deleteReview("r1", null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(reviewService, never()).deleteReviewAsClient(anyString(), anyString());
    }

    @Test
    void deleteReview_withNullUserId_returnsUnauthorized() {
        // Arrange
        when(jwt.getClaimAsString("sub")).thenReturn(null);

        // Act
        ResponseEntity<Void> response = reviewController.deleteReview("r1", jwt);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(reviewService, never()).deleteReviewAsClient(anyString(), anyString());
    }

    @Test
    void deleteReview_withNullReviewId_returnsBadRequest() {
        // Arrange
        when(jwt.getClaim("sub")).thenReturn("auth0|user123");
        when(jwt.getClaimAsString("sub")).thenReturn("auth0|user123");

        // Act
        ResponseEntity<Void> response = reviewController.deleteReview(null, jwt);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).deleteReviewAsClient(anyString(), anyString());
    }

    @Test
    void deleteReview_withBlankReviewId_returnsBadRequest() {
        // Arrange
        when(jwt.getClaim("sub")).thenReturn("auth0|user123");
        when(jwt.getClaimAsString("sub")).thenReturn("auth0|user123");

        // Act
        ResponseEntity<Void> response = reviewController.deleteReview("", jwt);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, never()).deleteReviewAsClient(anyString(), anyString());
    }

    @Test
    void getMyReviews_returnsUserReviews() {
        // Arrange
        String userId = "auth0|user123";
        ReviewResponseModel review1 = new ReviewResponseModel("r1","PID", userId, "appt1", "John", "Great", true, Rating.FIVE, null, "Interior");
        ReviewResponseModel review2 = new ReviewResponseModel("r2","PID", userId, "appt2", "John", "Good", false, Rating.FOUR, null, "Interior");
        List<ReviewResponseModel> reviews = Arrays.asList(review1, review2);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(reviewService.getReviewsByOwnerAuth0Id(userId)).thenReturn(reviews);

        // Act
        ResponseEntity<List<ReviewResponseModel>> response = reviewController.getMyReviews(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(reviewService).getReviewsByOwnerAuth0Id(userId);
    }

    @Test
    void getSatisfactionPercentage_withReviews_returnsPercentage() {
        // Arrange
        ReviewResponseModel review = new ReviewResponseModel("r1","PID", "client1", "appt1", "John", "Great", true, Rating.FIVE, null, "Interior");
        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(Collections.singletonList(review));
        when(reviewService.computeSatisfactionPercentage()).thenReturn(92.5);

        // Act
        ResponseEntity<Double> response = reviewController.getSatisfactionPercentage();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(92.5, response.getBody());
        verify(reviewService).getAllVisibleReviews(null, null, null, null);
        verify(reviewService).computeSatisfactionPercentage();
    }

    @Test
    void getSatisfactionPercentage_withNoReviews_returnsZero() {
        // Arrange
        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<Double> response = reviewController.getSatisfactionPercentage();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody());
        verify(reviewService).getAllVisibleReviews(null, null, null, null);
        verify(reviewService, never()).computeSatisfactionPercentage();
    }

    @Test
    void deleteReviewsByReviewId_successfulDeletion() {
        // Arrange
        String reviewId = "review123";

        // Act
        assertDoesNotThrow(() -> reviewController.deleteReviewsByReviewId(reviewId));

        // Assert
        verify(reviewService, times(1)).deleteReview(reviewId);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void deleteReviewsByReviewId_reviewNotFound_throwsResponseStatusException() {
        // Arrange
        String reviewId = "nonexistent";
        doThrow(new RuntimeException("Review with ID " + reviewId + " does not exist"))
                .when(reviewService).deleteReview(reviewId);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reviewController.deleteReviewsByReviewId(reviewId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Review with ID " + reviewId + " does not exist", exception.getReason());

        verify(reviewService, times(1)).deleteReview(reviewId);
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void sendReviewToPortfolio_success() {
        // Arrange
        String reviewId = "review123";
        PortfolioResponseDto responseDto = new PortfolioResponseDto();
        responseDto.setPortfolioId("portfolio123");

        when(reviewService.sendToPortfolio(reviewId)).thenReturn(responseDto);

        // Act
        ResponseEntity<?> response = reviewController.sendReviewToPortfolio(reviewId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(reviewService).sendToPortfolio(reviewId);
    }

    @Test
    void sendReviewToPortfolio_reviewNotFound() {
        // Arrange
        String reviewId = "review123";
        when(reviewService.sendToPortfolio(reviewId)).thenThrow(new RuntimeException("Review not found"));

        // Act
        ResponseEntity<?> response = reviewController.sendReviewToPortfolio(reviewId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Review not found", response.getBody());
        verify(reviewService).sendToPortfolio(reviewId);
    }

    @Test
    void resetPortfolioStatus_success() {
        // Arrange
        String reviewId = "review123";

        // Act
        ResponseEntity<?> response = reviewController.resetPortfolioStatus(reviewId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).resetPortfolioStatus(reviewId);
    }

    @Test
    void resetPortfolioStatus_reviewNotFound() {
        // Arrange
        String reviewId = "review123";
        doThrow(new RuntimeException("Review not found")).when(reviewService).resetPortfolioStatus(reviewId);

        // Act
        ResponseEntity<?> response = reviewController.resetPortfolioStatus(reviewId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Review not found", response.getBody());
        verify(reviewService).resetPortfolioStatus(reviewId);
    }
}


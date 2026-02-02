package org.example.vladtech.contact.datamapperlayer;

import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.data.Rating;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewNotificationEmailMapperTest {

    private final String adminEmail = "admin@example.com";
    private final ReviewNotificationEmailMapper mapper = new ReviewNotificationEmailMapper(adminEmail);

    @Test
    void toNotificationEmail_withValidReview_returnsContactEmail() {
        // Arrange
        ReviewRequestModel review = new ReviewRequestModel();
        review.setClientName("John Doe");
        review.setRating(Rating.FIVE);
        review.setComment("Excellent service!");

        // Act
        ContactEmail email = mapper.toNotificationEmail(review);

        // Assert
        assertNotNull(email);
        assertEquals(adminEmail, email.getDestinary());
        assertEquals("New review pending approval: ", email.getTitle());
        assertEquals("REVIEW_NOTIFICATION", email.getTemplateName());
        assertTrue(email.getBody().contains("Client: John Doe"));
        assertTrue(email.getBody().contains("Rating: FIVE"));
        assertTrue(email.getBody().contains("Comment: Excellent service!"));
        assertEquals("Log in as Admin to review and approve it.", email.getFooter());
        assertEquals("John Doe", email.getName());
        assertNotNull(email.getSentDate());
    }

    @Test
    void toNotificationEmail_withNullReview_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapper.toNotificationEmail(null));
        assertEquals("review cannot be null", exception.getMessage());
    }

    @Test
    void toNotificationEmail_withNullFieldsInReview_returnsContactEmailWithEmptyValues() {
        // Arrange
        ReviewRequestModel review = new ReviewRequestModel();
        review.setClientName(null);
        review.setRating(null);
        review.setComment(null);

        // Act
        ContactEmail email = mapper.toNotificationEmail(review);

        // Assert
        assertNotNull(email);
        assertEquals(adminEmail, email.getDestinary());
        assertTrue(email.getBody().contains("Client: "));
        assertTrue(email.getBody().contains("Rating: "));
        assertTrue(email.getBody().contains("Comment: "));
        assertEquals("", email.getName());
    }
}
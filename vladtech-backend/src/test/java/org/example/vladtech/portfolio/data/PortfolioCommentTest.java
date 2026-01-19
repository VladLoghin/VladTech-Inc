package org.example.vladtech.portfolio.data;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioCommentTest {

    @Test
    void testNoArgsConstructor() {
        // Act
        PortfolioComment comment = new PortfolioComment();

        // Assert
        assertNotNull(comment);
        assertNull(comment.getAuthorName());
        assertNull(comment.getAuthorUserId());
        assertNull(comment.getTimestamp());
        assertNull(comment.getText());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Instant now = Instant.now();

        // Act
        PortfolioComment comment = new PortfolioComment("John Doe", "auth0|12345", now, "Great work!");

        // Assert
        assertEquals("John Doe", comment.getAuthorName());
        assertEquals("auth0|12345", comment.getAuthorUserId());
        assertEquals(now, comment.getTimestamp());
        assertEquals("Great work!", comment.getText());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        PortfolioComment comment = new PortfolioComment();
        Instant timestamp = Instant.parse("2024-01-15T10:00:00Z");

        // Act
        comment.setAuthorName("Jane Smith");
        comment.setAuthorUserId("auth0|67890");
        comment.setTimestamp(timestamp);
        comment.setText("Excellent project!");

        // Assert
        assertEquals("Jane Smith", comment.getAuthorName());
        assertEquals("auth0|67890", comment.getAuthorUserId());
        assertEquals(timestamp, comment.getTimestamp());
        assertEquals("Excellent project!", comment.getText());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        Instant now = Instant.now();
        PortfolioComment comment1 = new PortfolioComment("John", "auth0|123", now, "Great!");
        PortfolioComment comment2 = new PortfolioComment("John", "auth0|123", now, "Great!");
        PortfolioComment comment3 = new PortfolioComment("Jane", "auth0|456", now, "Nice!");

        // Assert
        assertEquals(comment1, comment2);
        assertNotEquals(comment1, comment3);
        assertEquals(comment1.hashCode(), comment2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        Instant now = Instant.now();
        PortfolioComment comment = new PortfolioComment("John", "auth0|123", now, "Great!");

        // Act
        String result = comment.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("auth0|123"));
        assertTrue(result.contains("Great!"));
    }

    @Test
    void testWithDifferentTimestamps() {
        // Arrange
        Instant past = Instant.parse("2023-01-01T00:00:00Z");
        Instant present = Instant.parse("2024-06-15T12:00:00Z");
        Instant future = Instant.parse("2025-12-31T23:59:59Z");

        // Act
        PortfolioComment comment1 = new PortfolioComment("User1", "auth0|1", past, "Past comment");
        PortfolioComment comment2 = new PortfolioComment("User2", "auth0|2", present, "Present comment");
        PortfolioComment comment3 = new PortfolioComment("User3", "auth0|3", future, "Future comment");

        // Assert
        assertTrue(comment1.getTimestamp().isBefore(comment2.getTimestamp()));
        assertTrue(comment2.getTimestamp().isBefore(comment3.getTimestamp()));
    }

    @Test
    void testWithEmptyText() {
        // Act
        PortfolioComment comment = new PortfolioComment("User", "auth0|123", Instant.now(), "");

        // Assert
        assertEquals("", comment.getText());
    }

    @Test
    void testWithLongText() {
        // Arrange
        String longText = "This is a very long comment that contains a lot of text. ".repeat(10);

        // Act
        PortfolioComment comment = new PortfolioComment("User", "auth0|123", Instant.now(), longText);

        // Assert
        assertEquals(longText, comment.getText());
        assertTrue(comment.getText().length() > 100);
    }

    @Test
    void testTimestampPrecision() {
        // Arrange
        Instant timestamp1 = Instant.parse("2024-01-15T10:30:45.123456Z");
        Instant timestamp2 = Instant.parse("2024-01-15T10:30:45.123456Z");

        // Act
        PortfolioComment comment1 = new PortfolioComment("User", "auth0|123", timestamp1, "Text");
        PortfolioComment comment2 = new PortfolioComment("User", "auth0|123", timestamp2, "Text");

        // Assert
        assertEquals(comment1.getTimestamp(), comment2.getTimestamp());
    }

    @Test
    void testAuth0UserIdFormat() {
        // Test different Auth0 ID formats
        PortfolioComment comment1 = new PortfolioComment("User1", "auth0|123456", Instant.now(), "Comment");
        PortfolioComment comment2 = new PortfolioComment("User2", "google-oauth2|789012", Instant.now(), "Comment");
        PortfolioComment comment3 = new PortfolioComment("User3", "facebook|345678", Instant.now(), "Comment");

        // Assert
        assertTrue(comment1.getAuthorUserId().startsWith("auth0|"));
        assertTrue(comment2.getAuthorUserId().startsWith("google-oauth2|"));
        assertTrue(comment3.getAuthorUserId().startsWith("facebook|"));
    }
}


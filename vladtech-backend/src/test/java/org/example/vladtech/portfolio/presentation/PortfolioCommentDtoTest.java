package org.example.vladtech.portfolio.presentation;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioCommentDtoTest {

    @Test
    void testNoArgsConstructor() {
        // Act
        PortfolioCommentDto dto = new PortfolioCommentDto();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getAuthorName());
        assertNull(dto.getAuthorUserId());
        assertNull(dto.getTimestamp());
        assertNull(dto.getText());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Instant now = Instant.now();

        // Act
        PortfolioCommentDto dto = new PortfolioCommentDto("John Doe", "auth0|12345", now, "Great work!");

        // Assert
        assertEquals("John Doe", dto.getAuthorName());
        assertEquals("auth0|12345", dto.getAuthorUserId());
        assertEquals(now, dto.getTimestamp());
        assertEquals("Great work!", dto.getText());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        PortfolioCommentDto dto = new PortfolioCommentDto();
        Instant timestamp = Instant.parse("2024-01-15T10:00:00Z");

        // Act
        dto.setAuthorName("Jane Smith");
        dto.setAuthorUserId("auth0|67890");
        dto.setTimestamp(timestamp);
        dto.setText("Excellent project!");

        // Assert
        assertEquals("Jane Smith", dto.getAuthorName());
        assertEquals("auth0|67890", dto.getAuthorUserId());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals("Excellent project!", dto.getText());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        Instant now = Instant.now();
        PortfolioCommentDto dto1 = new PortfolioCommentDto("John", "auth0|123", now, "Great!");
        PortfolioCommentDto dto2 = new PortfolioCommentDto("John", "auth0|123", now, "Great!");
        PortfolioCommentDto dto3 = new PortfolioCommentDto("Jane", "auth0|456", now, "Nice!");

        // Assert
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        Instant now = Instant.now();
        PortfolioCommentDto dto = new PortfolioCommentDto("John", "auth0|123", now, "Great!");

        // Act
        String result = dto.toString();

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
        PortfolioCommentDto dto1 = new PortfolioCommentDto("User1", "auth0|1", past, "Past comment");
        PortfolioCommentDto dto2 = new PortfolioCommentDto("User2", "auth0|2", present, "Present comment");
        PortfolioCommentDto dto3 = new PortfolioCommentDto("User3", "auth0|3", future, "Future comment");

        // Assert
        assertTrue(dto1.getTimestamp().isBefore(dto2.getTimestamp()));
        assertTrue(dto2.getTimestamp().isBefore(dto3.getTimestamp()));
    }

    @Test
    void testWithEmptyText() {
        // Act
        PortfolioCommentDto dto = new PortfolioCommentDto("User", "auth0|123", Instant.now(), "");

        // Assert
        assertEquals("", dto.getText());
    }

    @Test
    void testWithLongText() {
        // Arrange
        String longText = "This is a very long comment that contains a lot of text. ".repeat(10);

        // Act
        PortfolioCommentDto dto = new PortfolioCommentDto("User", "auth0|123", Instant.now(), longText);

        // Assert
        assertEquals(longText, dto.getText());
        assertTrue(dto.getText().length() > 100);
    }

    @Test
    void testTimestampPrecision() {
        // Arrange
        Instant timestamp1 = Instant.parse("2024-01-15T10:30:45.123456Z");
        Instant timestamp2 = Instant.parse("2024-01-15T10:30:45.123456Z");

        // Act
        PortfolioCommentDto dto1 = new PortfolioCommentDto("User", "auth0|123", timestamp1, "Text");
        PortfolioCommentDto dto2 = new PortfolioCommentDto("User", "auth0|123", timestamp2, "Text");

        // Assert
        assertEquals(dto1.getTimestamp(), dto2.getTimestamp());
    }

    @Test
    void testAuth0UserIdFormat() {
        // Test different Auth0 ID formats
        PortfolioCommentDto dto1 = new PortfolioCommentDto("User1", "auth0|123456", Instant.now(), "Comment");
        PortfolioCommentDto dto2 = new PortfolioCommentDto("User2", "google-oauth2|789012", Instant.now(), "Comment");
        PortfolioCommentDto dto3 = new PortfolioCommentDto("User3", "facebook|345678", Instant.now(), "Comment");

        // Assert
        assertTrue(dto1.getAuthorUserId().startsWith("auth0|"));
        assertTrue(dto2.getAuthorUserId().startsWith("google-oauth2|"));
        assertTrue(dto3.getAuthorUserId().startsWith("facebook|"));
    }

    @Test
    void testAuthorNameVariations() {
        // Test short name
        PortfolioCommentDto dto1 = new PortfolioCommentDto("Jo", "auth0|1", Instant.now(), "Comment");
        assertEquals("Jo", dto1.getAuthorName());

        // Test long name
        PortfolioCommentDto dto2 = new PortfolioCommentDto("John Jacob Jingleheimer Schmidt", "auth0|2", Instant.now(), "Comment");
        assertEquals("John Jacob Jingleheimer Schmidt", dto2.getAuthorName());

        // Test name with special characters
        PortfolioCommentDto dto3 = new PortfolioCommentDto("O'Brien-Smith", "auth0|3", Instant.now(), "Comment");
        assertEquals("O'Brien-Smith", dto3.getAuthorName());
    }
}


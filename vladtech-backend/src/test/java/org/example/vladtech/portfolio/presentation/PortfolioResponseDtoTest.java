package org.example.vladtech.portfolio.presentation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioResponseDtoTest {

    @Test
    void testNoArgsConstructor() {
        // Act
        PortfolioResponseDto dto = new PortfolioResponseDto();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getPortfolioId());
        assertNull(dto.getTitle());
        assertNull(dto.getImageUrl());
        assertNull(dto.getRating());
        assertNull(dto.getComments());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        List<PortfolioCommentDto> comments = new ArrayList<>();
        comments.add(new PortfolioCommentDto("John", "auth0|123", java.time.Instant.now(), "Great!"));

        // Act
        PortfolioResponseDto dto = new PortfolioResponseDto(
            "p123",
            "Project A",
            "/images/project-a.jpg",
            4.5,
            comments
        );

        // Assert
        assertEquals("p123", dto.getPortfolioId());
        assertEquals("Project A", dto.getTitle());
        assertEquals("/images/project-a.jpg", dto.getImageUrl());
        assertEquals(4.5, dto.getRating());
        assertEquals(1, dto.getComments().size());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        PortfolioResponseDto dto = new PortfolioResponseDto();
        List<PortfolioCommentDto> comments = new ArrayList<>();
        comments.add(new PortfolioCommentDto("Jane", "auth0|456", java.time.Instant.now(), "Nice!"));

        // Act
        dto.setPortfolioId("p456");
        dto.setTitle("Project B");
        dto.setImageUrl("/images/project-b.jpg");
        dto.setRating(4.8);
        dto.setComments(comments);

        // Assert
        assertEquals("p456", dto.getPortfolioId());
        assertEquals("Project B", dto.getTitle());
        assertEquals("/images/project-b.jpg", dto.getImageUrl());
        assertEquals(4.8, dto.getRating());
        assertEquals(1, dto.getComments().size());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        PortfolioResponseDto dto1 = new PortfolioResponseDto("p1", "Project A", "/images/a.jpg", 4.5, null);
        PortfolioResponseDto dto2 = new PortfolioResponseDto("p1", "Project A", "/images/a.jpg", 4.5, null);
        PortfolioResponseDto dto3 = new PortfolioResponseDto("p2", "Project B", "/images/b.jpg", 3.8, null);

        // Assert
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        PortfolioResponseDto dto = new PortfolioResponseDto("p1", "Project A", "/images/a.jpg", 4.5, null);

        // Act
        String result = dto.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("p1"));
        assertTrue(result.contains("Project A"));
    }

    @Test
    void testWithNullComments() {
        // Act
        PortfolioResponseDto dto = new PortfolioResponseDto("p1", "Project A", "/images/a.jpg", 4.5, null);

        // Assert
        assertNull(dto.getComments());
    }

    @Test
    void testWithEmptyComments() {
        // Arrange
        List<PortfolioCommentDto> comments = new ArrayList<>();

        // Act
        PortfolioResponseDto dto = new PortfolioResponseDto("p1", "Project A", "/images/a.jpg", 4.5, comments);

        // Assert
        assertNotNull(dto.getComments());
        assertTrue(dto.getComments().isEmpty());
    }

    @Test
    void testWithMultipleComments() {
        // Arrange
        List<PortfolioCommentDto> comments = new ArrayList<>();
        comments.add(new PortfolioCommentDto("User1", "auth0|1", java.time.Instant.now(), "Comment 1"));
        comments.add(new PortfolioCommentDto("User2", "auth0|2", java.time.Instant.now(), "Comment 2"));
        comments.add(new PortfolioCommentDto("User3", "auth0|3", java.time.Instant.now(), "Comment 3"));

        // Act
        PortfolioResponseDto dto = new PortfolioResponseDto("p1", "Project A", "/images/a.jpg", 4.5, comments);

        // Assert
        assertEquals(3, dto.getComments().size());
    }

    @Test
    void testRatingBoundaries() {
        // Test minimum rating
        PortfolioResponseDto dto1 = new PortfolioResponseDto("p1", "Project", "/image.jpg", 0.0, null);
        assertEquals(0.0, dto1.getRating());

        // Test maximum rating
        PortfolioResponseDto dto2 = new PortfolioResponseDto("p2", "Project", "/image.jpg", 5.0, null);
        assertEquals(5.0, dto2.getRating());

        // Test decimal rating
        PortfolioResponseDto dto3 = new PortfolioResponseDto("p3", "Project", "/image.jpg", 4.75, null);
        assertEquals(4.75, dto3.getRating());
    }

    @Test
    void testTitleVariations() {
        // Test short title
        PortfolioResponseDto dto1 = new PortfolioResponseDto("p1", "A", "/image.jpg", 4.0, null);
        assertEquals("A", dto1.getTitle());

        // Test long title
        String longTitle = "This is a very long project title that describes the project in detail";
        PortfolioResponseDto dto2 = new PortfolioResponseDto("p2", longTitle, "/image.jpg", 4.0, null);
        assertEquals(longTitle, dto2.getTitle());

        // Test title with special characters
        PortfolioResponseDto dto3 = new PortfolioResponseDto("p3", "Project #1 - Phase 2", "/image.jpg", 4.0, null);
        assertEquals("Project #1 - Phase 2", dto3.getTitle());
    }

    @Test
    void testImageUrlFormats() {
        // Test relative URL
        PortfolioResponseDto dto1 = new PortfolioResponseDto("p1", "Project", "/images/project.jpg", 4.0, null);
        assertEquals("/images/project.jpg", dto1.getImageUrl());

        // Test absolute URL
        PortfolioResponseDto dto2 = new PortfolioResponseDto("p2", "Project", "https://example.com/images/project.jpg", 4.0, null);
        assertEquals("https://example.com/images/project.jpg", dto2.getImageUrl());

        // Test with different file extensions
        PortfolioResponseDto dto3 = new PortfolioResponseDto("p3", "Project", "/images/project.png", 4.0, null);
        assertTrue(dto3.getImageUrl().endsWith(".png"));
    }
}


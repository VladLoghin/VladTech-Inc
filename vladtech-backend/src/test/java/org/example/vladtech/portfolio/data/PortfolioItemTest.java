package org.example.vladtech.portfolio.data;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioItemTest {

    @Test
    void testNoArgsConstructor() {
        // Act
        PortfolioItem item = new PortfolioItem();

        // Assert
        assertNotNull(item);
        assertNull(item.getPortfolioId());
        assertNull(item.getTitle());
        assertNull(item.getImageUrl());
        assertNotNull(item.getComments());
        assertTrue(item.getComments().isEmpty());
        assertFalse(item.isArchived());
    }

    @Test
    void testFourParameterConstructor() {
        // Arrange
        List<PortfolioComment> comments = new ArrayList<>();
        comments.add(new PortfolioComment("John", "auth0|123", java.time.Instant.now(), "Great!"));

        // Act
        PortfolioItem item = new PortfolioItem("Project A", "/images/project-a.jpg", "Interior", comments);

        // Assert
        assertEquals("Project A", item.getTitle());
        assertEquals("/images/project-a.jpg", item.getImageUrl());
        assertEquals(1, item.getComments().size());
        assertEquals("John", item.getComments().get(0).getAuthorName());
        assertFalse(item.isArchived());
    }

    @Test
    void testFourParameterConstructorWithNullComments() {
        // Act
        PortfolioItem item = new PortfolioItem("Project A", "/images/project-a.jpg", "Interior", null);

        // Assert
        assertNotNull(item.getComments());
        assertTrue(item.getComments().isEmpty());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        List<PortfolioComment> comments = new ArrayList<>();
        comments.add(new PortfolioComment("Jane", "auth0|456", java.time.Instant.now(), "Amazing!"));

        // Act
        PortfolioItem item = new PortfolioItem("id123", "review-456", null, "Project B", "/images/project-b.jpg", "Interior", comments, false);

        // Assert
        assertEquals("id123", item.getPortfolioId());
        assertEquals("Project B", item.getTitle());
        assertEquals("/images/project-b.jpg", item.getImageUrl());
        assertEquals(1, item.getComments().size());
        assertFalse(item.isArchived());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        PortfolioItem item = new PortfolioItem();
        List<PortfolioComment> comments = new ArrayList<>();
        comments.add(new PortfolioComment("Bob", "auth0|789", java.time.Instant.now(), "Nice!"));

        // Act
        item.setPortfolioId("p1");
        item.setTitle("Project C");
        item.setImageUrl("/images/project-c.jpg");
        item.setComments(comments);

        // Assert
        assertEquals("p1", item.getPortfolioId());
        assertEquals("Project C", item.getTitle());
        assertEquals("/images/project-c.jpg", item.getImageUrl());
        assertEquals(1, item.getComments().size());
        assertEquals("Bob", item.getComments().get(0).getAuthorName());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        PortfolioItem item1 = new PortfolioItem("Project A", "/images/a.jpg", "Interior", new ArrayList<>());
        item1.setPortfolioId("p1");

        PortfolioItem item2 = new PortfolioItem("Project A", "/images/a.jpg", "Interior", new ArrayList<>());
        item2.setPortfolioId("p1");

        PortfolioItem item3 = new PortfolioItem("Project B", "/images/b.jpg", "Interior", new ArrayList<>());
        item3.setPortfolioId("p2");

        // Assert
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        PortfolioItem item = new PortfolioItem("Project A", "/images/a.jpg", "Interior", new ArrayList<>());
        item.setPortfolioId("p1");

        // Act
        String result = item.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("p1"));
        assertTrue(result.contains("Project A"));
    }

    @Test
    void testAddingMultipleComments() {
        // Arrange
        PortfolioItem item = new PortfolioItem();
        List<PortfolioComment> comments = new ArrayList<>();

        // Act
        comments.add(new PortfolioComment("User1", "auth0|1", java.time.Instant.now(), "Comment 1"));
        comments.add(new PortfolioComment("User2", "auth0|2", java.time.Instant.now(), "Comment 2"));
        comments.add(new PortfolioComment("User3", "auth0|3", java.time.Instant.now(), "Comment 3"));
        item.setComments(comments);

        // Assert
        assertEquals(3, item.getComments().size());
    }

    @Test
    void testEmptyCommentsList() {
        // Arrange
        List<PortfolioComment> emptyComments = new ArrayList<>();

        // Act
        PortfolioItem item = new PortfolioItem("Project", "/image.jpg", "Interior", emptyComments);

        // Assert
        assertNotNull(item.getComments());
        assertTrue(item.getComments().isEmpty());
    }
}

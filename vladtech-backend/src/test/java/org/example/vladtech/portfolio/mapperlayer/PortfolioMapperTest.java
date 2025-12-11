package org.example.vladtech.portfolio.mapperlayer;

import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.presentation.PortfolioCommentDto;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioMapperTest {

    private PortfolioMapper portfolioMapper;

    @BeforeEach
    void setUp() {
        portfolioMapper = new PortfolioMapper();
    }

    @Test
    void entityToResponseDto_WithValidItem_ShouldMapCorrectly() {
        // Arrange
        List<PortfolioComment> comments = List.of(
                new PortfolioComment("Sarah M.", "S", "3 hours ago", "Beautiful countertop!"),
                new PortfolioComment("John D.", "J", "1 hour ago", "Love the modern design.")
        );

        PortfolioItem portfolioItem = new PortfolioItem(
                "Modern Kitchen Counter",
                "/uploads/portfolio/kitchencounter.jpg",
                4.9,
                comments
        );
        portfolioItem.setPortfolioId("portfolio-123");

        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(portfolioItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo("portfolio-123");
        assertThat(result.getTitle()).isEqualTo("Modern Kitchen Counter");
        assertThat(result.getImageUrl()).isEqualTo("/uploads/portfolio/kitchencounter.jpg");
        assertThat(result.getRating()).isEqualTo(4.9);
        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo("Sarah M.");
        assertThat(result.getComments().get(0).getAuthorInitial()).isEqualTo("S");
        assertThat(result.getComments().get(0).getTimeAgo()).isEqualTo("3 hours ago");
        assertThat(result.getComments().get(0).getText()).isEqualTo("Beautiful countertop!");
    }

    @Test
    void entityToResponseDto_WithNullItem_ShouldReturnNull() {
        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void entityToResponseDto_WithEmptyComments_ShouldMapCorrectly() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Simple Office",
                "/uploads/portfolio/newoffice.jpg",
                4.5,
                new ArrayList<>()
        );
        portfolioItem.setPortfolioId("portfolio-456");

        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(portfolioItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo("portfolio-456");
        assertThat(result.getTitle()).isEqualTo("Simple Office");
        assertThat(result.getImageUrl()).isEqualTo("/uploads/portfolio/newoffice.jpg");
        assertThat(result.getRating()).isEqualTo(4.5);
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void entityToResponseDto_WithMultipleComments_ShouldMapAllComments() {
        // Arrange
        List<PortfolioComment> comments = List.of(
                new PortfolioComment("Alice W.", "A", "2 days ago", "Fantastic work!"),
                new PortfolioComment("Bob K.", "B", "1 day ago", "Very impressed."),
                new PortfolioComment("Carol T.", "C", "5 hours ago", "Outstanding quality.")
        );

        PortfolioItem portfolioItem = new PortfolioItem(
                "Luxury Bathroom",
                "/uploads/portfolio/newbathroom.jpg",
                4.8,
                comments
        );
        portfolioItem.setPortfolioId("portfolio-789");

        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(portfolioItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getComments()).hasSize(3);
        assertThat(result.getComments()).extracting(PortfolioCommentDto::getAuthorName)
                .containsExactly("Alice W.", "Bob K.", "Carol T.");
        assertThat(result.getComments()).extracting(PortfolioCommentDto::getAuthorInitial)
                .containsExactly("A", "B", "C");
        assertThat(result.getComments()).extracting(PortfolioCommentDto::getTimeAgo)
                .containsExactly("2 days ago", "1 day ago", "5 hours ago");
        assertThat(result.getComments()).extracting(PortfolioCommentDto::getText)
                .containsExactly("Fantastic work!", "Very impressed.", "Outstanding quality.");
    }

    @Test
    void entityToResponseDto_WithHighRating_ShouldMapCorrectly() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Perfect Kitchen",
                "/uploads/portfolio/kitchenremodel.jpg",
                5.0,
                List.of()
        );
        portfolioItem.setPortfolioId("portfolio-perfect");

        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(portfolioItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5.0);
        assertThat(result.getTitle()).isEqualTo("Perfect Kitchen");
    }

    @Test
    void entityToResponseDto_WithLowRating_ShouldMapCorrectly() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Basic Renovation",
                "/uploads/portfolio/basic.jpg",
                3.5,
                List.of()
        );
        portfolioItem.setPortfolioId("portfolio-basic");

        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(portfolioItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(3.5);
    }

    @Test
    void entityToResponseDto_ShouldPreserveAllFields() {
        // Arrange
        PortfolioComment comment = new PortfolioComment(
                "Test User",
                "T",
                "just now",
                "Test comment text"
        );

        PortfolioItem portfolioItem = new PortfolioItem(
                "Test Title",
                "/test/url.jpg",
                4.7,
                List.of(comment)
        );
        portfolioItem.setPortfolioId("test-id");

        // Act
        PortfolioResponseDto result = portfolioMapper.entityToResponseDto(portfolioItem);

        // Assert
        assertThat(result.getPortfolioId()).isEqualTo("test-id");
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getImageUrl()).isEqualTo("/test/url.jpg");
        assertThat(result.getRating()).isEqualTo(4.7);
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo("Test User");
        assertThat(result.getComments().get(0).getAuthorInitial()).isEqualTo("T");
        assertThat(result.getComments().get(0).getTimeAgo()).isEqualTo("just now");
        assertThat(result.getComments().get(0).getText()).isEqualTo("Test comment text");
    }
}


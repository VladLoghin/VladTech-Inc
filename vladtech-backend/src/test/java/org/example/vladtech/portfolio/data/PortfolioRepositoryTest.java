package org.example.vladtech.portfolio.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class PortfolioRepositoryTest {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @BeforeEach
    void setUp() {
        portfolioRepository.deleteAll();
    }

    @Test
    void save_ShouldPersistPortfolioItem() {
        // Arrange
        Instant now = Instant.now();
        PortfolioItem portfolioItem = new PortfolioItem(
                "Modern Kitchen Counter",
                "/uploads/portfolio/kitchencounter.jpg",
                4.9,
                List.of(new PortfolioComment("Sarah M.", "auth0|user1", now.minusSeconds(10800), "Beautiful!"))
        );

        // Act
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getPortfolioId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Modern Kitchen Counter");
        assertThat(saved.getImageUrl()).isEqualTo("/uploads/portfolio/kitchencounter.jpg");
        assertThat(saved.getRating()).isEqualTo(4.9);
        assertThat(saved.getComments()).hasSize(1);
    }

    @Test
    void findById_WhenItemExists_ShouldReturnItem() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Luxury Bathroom",
                "/uploads/portfolio/newbathroom.jpg",
                4.8,
                List.of()
        );
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Act
        Optional<PortfolioItem> found = portfolioRepository.findById(saved.getPortfolioId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Luxury Bathroom");
        assertThat(found.get().getImageUrl()).isEqualTo("/uploads/portfolio/newbathroom.jpg");
        assertThat(found.get().getRating()).isEqualTo(4.8);
    }

    @Test
    void findById_WhenItemDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<PortfolioItem> found = portfolioRepository.findById("non-existent-id");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        // Arrange
        PortfolioItem item1 = new PortfolioItem(
                "Kitchen Remodel",
                "/uploads/portfolio/kitchen.jpg",
                5.0,
                List.of()
        );
        PortfolioItem item2 = new PortfolioItem(
                "Bathroom Renovation",
                "/uploads/portfolio/bathroom.jpg",
                4.7,
                List.of()
        );
        portfolioRepository.saveAll(List.of(item1, item2));

        // Act
        List<PortfolioItem> allItems = portfolioRepository.findAll();

        // Assert
        assertThat(allItems).hasSize(2);
        assertThat(allItems).extracting(PortfolioItem::getTitle)
                .containsExactlyInAnyOrder("Kitchen Remodel", "Bathroom Renovation");
    }

    @Test
    void findAll_WhenEmpty_ShouldReturnEmptyList() {
        // Act
        List<PortfolioItem> allItems = portfolioRepository.findAll();

        // Assert
        assertThat(allItems).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveItem() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Office Space",
                "/uploads/portfolio/office.jpg",
                4.5,
                List.of()
        );
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Act
        portfolioRepository.deleteById(saved.getPortfolioId());

        // Assert
        Optional<PortfolioItem> found = portfolioRepository.findById(saved.getPortfolioId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAll_ShouldRemoveAllItems() {
        // Arrange
        PortfolioItem item1 = new PortfolioItem("Item 1", "/url1.jpg", 4.5, List.of());
        PortfolioItem item2 = new PortfolioItem("Item 2", "/url2.jpg", 4.8, List.of());
        portfolioRepository.saveAll(List.of(item1, item2));

        // Act
        portfolioRepository.deleteAll();

        // Assert
        List<PortfolioItem> allItems = portfolioRepository.findAll();
        assertThat(allItems).isEmpty();
    }

    @Test
    void save_WithComments_ShouldPersistComments() {
        // Arrange
        Instant now = Instant.now();
        List<PortfolioComment> comments = List.of(
                new PortfolioComment("Alice W.", "auth0|user3", now.minusSeconds(172800), "Great work!"),
                new PortfolioComment("Bob K.", "auth0|user4", now.minusSeconds(86400), "Excellent!")
        );
        PortfolioItem portfolioItem = new PortfolioItem(
                "Premium Kitchen",
                "/uploads/portfolio/premium.jpg",
                5.0,
                comments
        );

        // Act
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Assert
        Optional<PortfolioItem> found = portfolioRepository.findById(saved.getPortfolioId());
        assertThat(found).isPresent();
        assertThat(found.get().getComments()).hasSize(2);
        assertThat(found.get().getComments().get(0).getAuthorName()).isEqualTo("Alice W.");
        assertThat(found.get().getComments().get(1).getAuthorName()).isEqualTo("Bob K.");
    }

    @Test
    void update_ShouldModifyExistingItem() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Original Title",
                "/original.jpg",
                4.0,
                List.of()
        );
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Act
        saved.setTitle("Updated Title");
        saved.setRating(4.9);
        portfolioRepository.save(saved);

        // Assert
        Optional<PortfolioItem> found = portfolioRepository.findById(saved.getPortfolioId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Updated Title");
        assertThat(found.get().getRating()).isEqualTo(4.9);
    }

    @Test
    void count_ShouldReturnCorrectNumber() {
        // Arrange
        PortfolioItem item1 = new PortfolioItem("Item 1", "/url1.jpg", 4.5, List.of());
        PortfolioItem item2 = new PortfolioItem("Item 2", "/url2.jpg", 4.8, List.of());
        PortfolioItem item3 = new PortfolioItem("Item 3", "/url3.jpg", 4.6, List.of());
        portfolioRepository.saveAll(List.of(item1, item2, item3));

        // Act
        long count = portfolioRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }
}


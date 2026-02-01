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
                null,
                List.of(new PortfolioComment("Sarah M.", "auth0|user1", now.minusSeconds(10800), "Beautiful!"))
        );

        // Act
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getPortfolioId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Modern Kitchen Counter");
        assertThat(saved.getImageUrl()).isEqualTo("/uploads/portfolio/kitchencounter.jpg");
        assertThat(saved.getComments()).hasSize(1);
    }

    @Test
    void findById_WhenItemExists_ShouldReturnItem() {
        // Arrange
        PortfolioItem portfolioItem = new PortfolioItem(
                "Luxury Bathroom",
                "/uploads/portfolio/newbathroom.jpg",
                null,
                List.of()
        );
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Act
        Optional<PortfolioItem> found = portfolioRepository.findById(saved.getPortfolioId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Luxury Bathroom");
        assertThat(found.get().getImageUrl()).isEqualTo("/uploads/portfolio/newbathroom.jpg");
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
                null,
                List.of()
        );
        PortfolioItem item2 = new PortfolioItem(
                "Bathroom Renovation",
                "/uploads/portfolio/bathroom.jpg",
                null,
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
                null,
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
        PortfolioItem item1 = new PortfolioItem("Item 1", "/url1.jpg", null, List.of());
        PortfolioItem item2 = new PortfolioItem("Item 2", "/url2.jpg", null, List.of());
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
                null,
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
                null,
                List.of()
        );
        PortfolioItem saved = portfolioRepository.save(portfolioItem);

        // Act
        saved.setTitle("Updated Title");
        portfolioRepository.save(saved);

        // Assert
        Optional<PortfolioItem> found = portfolioRepository.findById(saved.getPortfolioId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void count_ShouldReturnCorrectNumber() {
        // Arrange
        PortfolioItem item1 = new PortfolioItem("Item 1", "/url1.jpg", null, List.of());
        PortfolioItem item2 = new PortfolioItem("Item 2", "/url2.jpg", null, List.of());
        PortfolioItem item3 = new PortfolioItem("Item 3", "/url3.jpg", null, List.of());
        portfolioRepository.saveAll(List.of(item1, item2, item3));

        // Act
        long count = portfolioRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void findByType_WhenTypeExists_ShouldReturnMatchingItems() {
        // Arrange
        PortfolioItem kitchenItem1 = new PortfolioItem(
                "Modern Kitchen",
                "/kitchen1.jpg",
                "Kitchen",
                List.of()
        );
        PortfolioItem kitchenItem2 = new PortfolioItem(
                "Classic Kitchen",
                "/kitchen2.jpg",
                "Kitchen",
                List.of()
        );
        PortfolioItem bathroomItem = new PortfolioItem(
                "Luxury Bathroom",
                "/bathroom.jpg",
                "Bathroom",
                List.of()
        );
        portfolioRepository.saveAll(List.of(kitchenItem1, kitchenItem2, bathroomItem));

        // Act
        List<PortfolioItem> result = portfolioRepository.findByType("Kitchen");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PortfolioItem::getTitle)
                .containsExactlyInAnyOrder("Modern Kitchen", "Classic Kitchen");
        assertThat(result).allMatch(item -> "Kitchen".equals(item.getType()));
    }

    @Test
    void findByType_WhenNoMatchingType_ShouldReturnEmptyList() {
        // Arrange
        PortfolioItem kitchenItem = new PortfolioItem(
                "Kitchen Project",
                "/kitchen.jpg",
                "Kitchen",
                List.of()
        );
        portfolioRepository.save(kitchenItem);

        // Act
        List<PortfolioItem> result = portfolioRepository.findByType("Exterior");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByType_WithMultipleTypes_ShouldReturnOnlyMatchingType() {
        // Arrange
        PortfolioItem interiorItem = new PortfolioItem(
                "Living Room",
                "/living.jpg",
                "Interior",
                List.of()
        );
        PortfolioItem bathroomItem = new PortfolioItem(
                "Bathroom Remodel",
                "/bathroom.jpg",
                "Bathroom",
                List.of()
        );
        PortfolioItem exteriorItem = new PortfolioItem(
                "Garden Design",
                "/garden.jpg",
                "Exterior",
                List.of()
        );
        PortfolioItem kitchenItem = new PortfolioItem(
                "Kitchen Renovation",
                "/kitchen.jpg",
                "Kitchen",
                List.of()
        );
        portfolioRepository.saveAll(List.of(interiorItem, bathroomItem, exteriorItem, kitchenItem));

        // Act
        List<PortfolioItem> bathroomResults = portfolioRepository.findByType("Bathroom");
        List<PortfolioItem> exteriorResults = portfolioRepository.findByType("Exterior");
        List<PortfolioItem> kitchenResults = portfolioRepository.findByType("Kitchen");

        // Assert
        assertThat(bathroomResults).hasSize(1);
        assertThat(bathroomResults.get(0).getTitle()).isEqualTo("Bathroom Remodel");

        assertThat(exteriorResults).hasSize(1);
        assertThat(exteriorResults.get(0).getTitle()).isEqualTo("Garden Design");

        assertThat(kitchenResults).hasSize(1);
        assertThat(kitchenResults.get(0).getTitle()).isEqualTo("Kitchen Renovation");
    }

    @Test
    void findByType_WhenTypeIsNull_ShouldReturnEmpty() {
        // Arrange
        PortfolioItem item = new PortfolioItem(
                "Test Item",
                "/test.jpg",
                "Kitchen",
                List.of()
        );
        portfolioRepository.save(item);

        // Act
        List<PortfolioItem> result = portfolioRepository.findByType(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByType_CaseSensitive_ShouldMatchExactCase() {
        // Arrange
        PortfolioItem item = new PortfolioItem(
                "Kitchen Project",
                "/kitchen.jpg",
                "Kitchen",
                List.of()
        );
        portfolioRepository.save(item);

        // Act
        List<PortfolioItem> result = portfolioRepository.findByType("kitchen");

        // Assert - MongoDB findByType is case-sensitive by default
        assertThat(result).isEmpty();
    }
}


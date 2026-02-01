package org.example.vladtech.portfolio.business;

import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.example.vladtech.portfolio.mapperlayer.PortfolioMapper;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException;
import org.example.vladtech.portfolio.presentation.PortfolioCommentDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioMapper portfolioMapper;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private PortfolioItem portfolioItem1;
    private PortfolioItem portfolioItem2;
    private PortfolioResponseDto responseDto1;
    private PortfolioResponseDto responseDto2;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        portfolioItem1 = new PortfolioItem(
                "Modern Kitchen Counter",
                "/uploads/portfolio/kitchencounter.jpg",
                null,
                List.of(
                        new PortfolioComment("Sarah M.", "auth0|user1", now.minusSeconds(10800),
                                "Beautiful countertop!")));
        portfolioItem1.setPortfolioId("portfolio-id-1");

        portfolioItem2 = new PortfolioItem(
                "Complete Kitchen Remodel",
                "/uploads/portfolio/kitchenremodel.jpg",
                null,
                List.of(
                        new PortfolioComment("Emma L.", "auth0|user2", now.minusSeconds(18000),
                                "Amazing transformation!")));
        portfolioItem2.setPortfolioId("portfolio-id-2");

        responseDto1 = new PortfolioResponseDto();
        responseDto1.setPortfolioId("portfolio-id-1");
        responseDto1.setTitle("Modern Kitchen Counter");
        responseDto1.setImageUrl("/uploads/portfolio/kitchencounter.jpg");

        responseDto2 = new PortfolioResponseDto();
        responseDto2.setPortfolioId("portfolio-id-2");
        responseDto2.setTitle("Complete Kitchen Remodel");
        responseDto2.setImageUrl("/uploads/portfolio/kitchenremodel.jpg");
    }

    @Test
    void getAllPortfolioItems_ShouldReturnAllItems() {
        // Arrange
        List<PortfolioItem> portfolioItems = List.of(portfolioItem1, portfolioItem2);
        when(portfolioRepository.findAll()).thenReturn(portfolioItems);
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);
        when(portfolioMapper.entityToResponseDto(portfolioItem2)).thenReturn(responseDto2);

        // Act
        List<PortfolioResponseDto> result = portfolioService.getAllPortfolioItems();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPortfolioId()).isEqualTo("portfolio-id-1");
        assertThat(result.get(0).getTitle()).isEqualTo("Modern Kitchen Counter");
        assertThat(result.get(1).getPortfolioId()).isEqualTo("portfolio-id-2");
        assertThat(result.get(1).getTitle()).isEqualTo("Complete Kitchen Remodel");

        verify(portfolioRepository, times(1)).findAll();
        verify(portfolioMapper, times(2)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getAllPortfolioItems_WhenNoItems_ShouldReturnEmptyList() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of());

        // Act
        List<PortfolioResponseDto> result = portfolioService.getAllPortfolioItems();

        // Assert
        assertThat(result).isEmpty();
        verify(portfolioRepository, times(1)).findAll();
        verify(portfolioMapper, never()).entityToResponseDto(any());
    }

    @Test
    void getPortfolioItemById_WhenItemExists_ShouldReturnItem() {
        // Arrange
        String portfolioId = "portfolio-id-1";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);

        // Act
        PortfolioResponseDto result = portfolioService.getPortfolioItemById(portfolioId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo("portfolio-id-1");
        assertThat(result.getTitle()).isEqualTo("Modern Kitchen Counter");
        assertThat(result.getImageUrl()).isEqualTo("/uploads/portfolio/kitchencounter.jpg");

        verify(portfolioRepository, times(1)).findById(portfolioId);
        verify(portfolioMapper, times(1)).entityToResponseDto(portfolioItem1);
    }

    @Test
    void getPortfolioItemById_WhenItemDoesNotExist_ShouldThrowException() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(portfolioRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> portfolioService.getPortfolioItemById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Portfolio item not found with id: " + nonExistentId);

        verify(portfolioRepository, times(1)).findById(nonExistentId);
        verify(portfolioMapper, never()).entityToResponseDto(any());
    }

    @Test
    void getAllPortfolioItems_ShouldMapAllItemsCorrectly() {
        // Arrange
        when(portfolioRepository.findAll()).thenReturn(List.of(portfolioItem1, portfolioItem2));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);
        when(portfolioMapper.entityToResponseDto(portfolioItem2)).thenReturn(responseDto2);

        // Act
        List<PortfolioResponseDto> result = portfolioService.getAllPortfolioItems();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PortfolioResponseDto::getTitle)
                .containsExactly("Modern Kitchen Counter", "Complete Kitchen Remodel");
    }

    @Test
    void getPortfolioItemById_ShouldHandleValidId() {
        // Arrange
        String portfolioId = "valid-portfolio-id";
        portfolioItem1.setPortfolioId(portfolioId);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);

        // Act
        PortfolioResponseDto result = portfolioService.getPortfolioItemById(portfolioId);

        // Assert
        assertThat(result).isNotNull();
        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioMapper).entityToResponseDto(portfolioItem1);
    }

    @Test
    void addComment_ShouldAddCommentSuccessfully() {
        // Arrange
        String portfolioId = "portfolio-id-1";
        String commentText = "Great work!";
        String userId = "auth0|user123";
        String userName = "Test User";

        PortfolioItem portfolioItemWithComments = new PortfolioItem(
                "Modern Kitchen Counter",
                "/uploads/portfolio/kitchencounter.jpg",
                null,
                new ArrayList<>());
        portfolioItemWithComments.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItemWithComments));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(portfolioItemWithComments);

        // Act
        PortfolioCommentDto result = portfolioService.addComment(portfolioId, commentText, userId, userName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAuthorName()).isEqualTo(userName);
        assertThat(result.getAuthorUserId()).isEqualTo(userId);
        assertThat(result.getText()).isEqualTo(commentText);
        assertThat(result.getTimestamp()).isNotNull();

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).save(portfolioItemWithComments);
    }

    @Test
    void addComment_WhenPortfolioNotFound_ShouldThrowException() {
        // Arrange
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> portfolioService.addComment(portfolioId, "comment", "userId", "userName"))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio item not found with id: " + portfolioId);

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void createPortfolioItem_ShouldCreateSuccessfully() {
        // Arrange
        String title = "New Kitchen Project";
        String imageUrl = "/uploads/portfolio/new-kitchen.jpg";
        Double rating = 4.5;

        PortfolioItem savedItem = new PortfolioItem();
        savedItem.setPortfolioId("new-portfolio-id");
        savedItem.setTitle(title);
        savedItem.setImageUrl(imageUrl);

        PortfolioResponseDto expectedResponse = new PortfolioResponseDto();
        expectedResponse.setPortfolioId("new-portfolio-id");
        expectedResponse.setTitle(title);
        expectedResponse.setImageUrl(imageUrl);

        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(savedItem);
        when(portfolioMapper.entityToResponseDto(savedItem)).thenReturn(expectedResponse);

        // Act
        PortfolioResponseDto result = portfolioService.createPortfolioItem(title, imageUrl, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo("new-portfolio-id");
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);

        verify(portfolioRepository).save(any(PortfolioItem.class));
        verify(portfolioMapper).entityToResponseDto(savedItem);
    }

    @Test
    void deletePortfolioItem_ShouldDeleteSuccessfully() {
        // Arrange
        String portfolioId = "portfolio-to-delete";
        PortfolioItem itemToDelete = new PortfolioItem(
                "Item to Delete",
                "/uploads/portfolio/delete-me.jpg",
                null,
                new ArrayList<>());
        itemToDelete.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(itemToDelete));

        // Act
        portfolioService.deletePortfolioItem(portfolioId);

        // Assert
        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).delete(itemToDelete);
    }

    @Test
    void deletePortfolioItem_WhenPortfolioNotFound_ShouldThrowException() {
        // Arrange
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> portfolioService.deletePortfolioItem(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio item not found with id: " + portfolioId);

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository, never()).delete(any());
    }

    @Test
    void getPortfolioItemsByType_WhenTypeExists_ShouldReturnMatchingItems() {
        // Arrange
        String type = "Kitchen";
        List<PortfolioItem> kitchenItems = List.of(portfolioItem1, portfolioItem2);

        when(portfolioRepository.findByType(type)).thenReturn(kitchenItems);
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);
        when(portfolioMapper.entityToResponseDto(portfolioItem2)).thenReturn(responseDto2);

        // Act
        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(type);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(responseDto1, responseDto2);
        verify(portfolioRepository).findByType(type);
        verify(portfolioMapper, times(2)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getPortfolioItemsByType_WhenNoMatchingType_ShouldReturnEmptyList() {
        // Arrange
        String type = "Exterior";
        when(portfolioRepository.findByType(type)).thenReturn(List.of());

        // Act
        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(type);

        // Assert
        assertThat(result).isEmpty();
        verify(portfolioRepository).findByType(type);
        verify(portfolioMapper, never()).entityToResponseDto(any());
    }

    @Test
    void getPortfolioItemsByType_WithDifferentTypes_ShouldReturnCorrectItems() {
        // Arrange
        String bathroomType = "Bathroom";
        PortfolioItem bathroomItem = new PortfolioItem(
                "Luxury Bathroom",
                "/bathroom.jpg",
                bathroomType,
                List.of()
        );
        bathroomItem.setPortfolioId("bathroom-id");

        PortfolioResponseDto bathroomDto = new PortfolioResponseDto();
        bathroomDto.setPortfolioId("bathroom-id");
        bathroomDto.setTitle("Luxury Bathroom");
        bathroomDto.setType(bathroomType);

        when(portfolioRepository.findByType(bathroomType)).thenReturn(List.of(bathroomItem));
        when(portfolioMapper.entityToResponseDto(bathroomItem)).thenReturn(bathroomDto);

        // Act
        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(bathroomType);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(bathroomType);
        assertThat(result.get(0).getTitle()).isEqualTo("Luxury Bathroom");
        verify(portfolioRepository).findByType(bathroomType);
    }

    @Test
    void getPortfolioItemsByType_ShouldMapAllItemsCorrectly() {
        // Arrange
        String type = "Interior";
        PortfolioItem item1 = new PortfolioItem("Living Room", "/living.jpg", type, List.of());
        PortfolioItem item2 = new PortfolioItem("Bedroom", "/bedroom.jpg", type, List.of());
        PortfolioItem item3 = new PortfolioItem("Office", "/office.jpg", type, List.of());

        PortfolioResponseDto dto1 = new PortfolioResponseDto();
        dto1.setTitle("Living Room");
        dto1.setType(type);

        PortfolioResponseDto dto2 = new PortfolioResponseDto();
        dto2.setTitle("Bedroom");
        dto2.setType(type);

        PortfolioResponseDto dto3 = new PortfolioResponseDto();
        dto3.setTitle("Office");
        dto3.setType(type);

        when(portfolioRepository.findByType(type)).thenReturn(List.of(item1, item2, item3));
        when(portfolioMapper.entityToResponseDto(item1)).thenReturn(dto1);
        when(portfolioMapper.entityToResponseDto(item2)).thenReturn(dto2);
        when(portfolioMapper.entityToResponseDto(item3)).thenReturn(dto3);

        // Act
        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(type);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(PortfolioResponseDto::getTitle)
                .containsExactly("Living Room", "Bedroom", "Office");
        assertThat(result).allMatch(dto -> type.equals(dto.getType()));
        verify(portfolioRepository).findByType(type);
        verify(portfolioMapper, times(3)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getPortfolioItemsByType_WithNullType_ShouldCallRepository() {
        // Arrange
        when(portfolioRepository.findByType(null)).thenReturn(List.of());

        // Act
        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(null);

        // Assert
        assertThat(result).isEmpty();
        verify(portfolioRepository).findByType(null);
    }
}

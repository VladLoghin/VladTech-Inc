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

    // ============ getAllPortfolioItems ============

    @Test
    void getAllPortfolioItems_ShouldReturnAllNonArchivedItems() {
        List<PortfolioItem> portfolioItems = List.of(portfolioItem1, portfolioItem2);
        when(portfolioRepository.findByArchivedFalse()).thenReturn(portfolioItems);
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);
        when(portfolioMapper.entityToResponseDto(portfolioItem2)).thenReturn(responseDto2);

        List<PortfolioResponseDto> result = portfolioService.getAllPortfolioItems();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPortfolioId()).isEqualTo("portfolio-id-1");
        assertThat(result.get(1).getPortfolioId()).isEqualTo("portfolio-id-2");
        verify(portfolioRepository, times(1)).findByArchivedFalse();
        verify(portfolioMapper, times(2)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getAllPortfolioItems_WhenNoItems_ShouldReturnEmptyList() {
        when(portfolioRepository.findByArchivedFalse()).thenReturn(List.of());

        List<PortfolioResponseDto> result = portfolioService.getAllPortfolioItems();

        assertThat(result).isEmpty();
        verify(portfolioRepository, times(1)).findByArchivedFalse();
        verify(portfolioMapper, never()).entityToResponseDto(any());
    }

    // ============ getPortfolioItemById ============

    @Test
    void getPortfolioItemById_WhenItemExists_ShouldReturnItem() {
        String portfolioId = "portfolio-id-1";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);

        PortfolioResponseDto result = portfolioService.getPortfolioItemById(portfolioId);

        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo("portfolio-id-1");
        assertThat(result.getTitle()).isEqualTo("Modern Kitchen Counter");
        verify(portfolioRepository, times(1)).findById(portfolioId);
    }

    @Test
    void getPortfolioItemById_WhenItemDoesNotExist_ShouldThrowException() {
        String nonExistentId = "non-existent-id";
        when(portfolioRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getPortfolioItemById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Portfolio item not found with id: " + nonExistentId);
        verify(portfolioMapper, never()).entityToResponseDto(any());
    }

    // ============ addComment ============

    @Test
    void addComment_ShouldAddCommentSuccessfully() {
        String portfolioId = "portfolio-id-1";
        PortfolioItem item = new PortfolioItem("Title", "/img.jpg", null, new ArrayList<>());
        item.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(item));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(item);

        PortfolioCommentDto result = portfolioService.addComment(portfolioId, "Great work!", "auth0|user123", "Test User");

        assertThat(result).isNotNull();
        assertThat(result.getAuthorName()).isEqualTo("Test User");
        assertThat(result.getText()).isEqualTo("Great work!");
        assertThat(result.getTimestamp()).isNotNull();
        verify(portfolioRepository).save(item);
    }

    @Test
    void addComment_WhenPortfolioNotFound_ShouldThrowException() {
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.addComment(portfolioId, "comment", "userId", "userName"))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).save(any());
    }

    // ============ createPortfolioItem ============

    @Test
    void createPortfolioItem_WithSingleImage_ShouldCreateSuccessfully() {
        String title = "New Kitchen Project";
        String imageUrl = "/uploads/portfolio/new-kitchen.jpg";
        List<String> imageUrls = List.of(imageUrl);

        PortfolioItem savedItem = new PortfolioItem();
        savedItem.setPortfolioId("new-portfolio-id");
        savedItem.setTitle(title);
        savedItem.setImageUrl(imageUrl);
        savedItem.setImageUrls(imageUrls);

        PortfolioResponseDto expectedResponse = new PortfolioResponseDto();
        expectedResponse.setPortfolioId("new-portfolio-id");
        expectedResponse.setTitle(title);
        expectedResponse.setImageUrl(imageUrl);
        expectedResponse.setImageUrls(imageUrls);

        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(savedItem);
        when(portfolioMapper.entityToResponseDto(savedItem)).thenReturn(expectedResponse);

        PortfolioResponseDto result = portfolioService.createPortfolioItem(title, imageUrl, imageUrls, null);

        assertThat(result).isNotNull();
        assertThat(result.getPortfolioId()).isEqualTo("new-portfolio-id");
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getImageUrls()).containsExactly(imageUrl);
        verify(portfolioRepository).save(any(PortfolioItem.class));
    }

    @Test
    void createPortfolioItem_WithMultipleImages_ShouldCreateSuccessfully() {
        String title = "Multi-Image Project";
        String img1 = "/uploads/portfolio/img1.jpg";
        String img2 = "/uploads/portfolio/img2.jpg";
        String img3 = "/uploads/portfolio/img3.jpg";
        List<String> imageUrls = List.of(img1, img2, img3);

        PortfolioItem savedItem = new PortfolioItem();
        savedItem.setPortfolioId("multi-img-id");
        savedItem.setTitle(title);
        savedItem.setImageUrl(img1);
        savedItem.setImageUrls(imageUrls);

        PortfolioResponseDto expectedResponse = new PortfolioResponseDto();
        expectedResponse.setPortfolioId("multi-img-id");
        expectedResponse.setTitle(title);
        expectedResponse.setImageUrls(imageUrls);

        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(savedItem);
        when(portfolioMapper.entityToResponseDto(savedItem)).thenReturn(expectedResponse);

        PortfolioResponseDto result = portfolioService.createPortfolioItem(title, img1, imageUrls, "Kitchen");

        assertThat(result).isNotNull();
        assertThat(result.getImageUrls()).hasSize(3);
        verify(portfolioRepository).save(any(PortfolioItem.class));
    }

    @Test
    void createPortfolioItem_WithNullImageUrls_ShouldFallbackToImageUrl() {
        String title = "Fallback Project";
        String imageUrl = "/uploads/portfolio/fallback.jpg";

        PortfolioItem savedItem = new PortfolioItem();
        savedItem.setPortfolioId("fallback-id");
        savedItem.setTitle(title);
        savedItem.setImageUrl(imageUrl);
        savedItem.setImageUrls(List.of(imageUrl));

        PortfolioResponseDto expectedResponse = new PortfolioResponseDto();
        expectedResponse.setPortfolioId("fallback-id");
        expectedResponse.setImageUrls(List.of(imageUrl));

        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(savedItem);
        when(portfolioMapper.entityToResponseDto(savedItem)).thenReturn(expectedResponse);

        PortfolioResponseDto result = portfolioService.createPortfolioItem(title, imageUrl, null, "Interior");

        assertThat(result).isNotNull();
        assertThat(result.getImageUrls()).containsExactly(imageUrl);
    }

    // ============ updatePortfolioItem ============

    @Test
    void updatePortfolioItem_ShouldUpdateAllFields() {
        String portfolioId = "portfolio-id-1";
        String newTitle = "Updated Title";
        List<String> newImageUrls = List.of("/uploads/new1.jpg", "/uploads/new2.jpg");
        String newType = "Bathroom";

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(portfolioItem1);

        PortfolioResponseDto expectedResponse = new PortfolioResponseDto();
        expectedResponse.setPortfolioId(portfolioId);
        expectedResponse.setTitle(newTitle);
        expectedResponse.setImageUrls(newImageUrls);
        expectedResponse.setType(newType);
        when(portfolioMapper.entityToResponseDto(any())).thenReturn(expectedResponse);

        PortfolioResponseDto result = portfolioService.updatePortfolioItem(portfolioId, newTitle, newImageUrls, newType);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(newTitle);
        assertThat(result.getImageUrls()).hasSize(2);
        assertThat(result.getType()).isEqualTo(newType);
        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).save(any(PortfolioItem.class));
    }

    @Test
    void updatePortfolioItem_WithPartialUpdate_ShouldOnlyUpdateProvidedFields() {
        String portfolioId = "portfolio-id-1";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(portfolioItem1);

        PortfolioResponseDto expectedResponse = new PortfolioResponseDto();
        expectedResponse.setPortfolioId(portfolioId);
        expectedResponse.setTitle("Updated Title Only");
        when(portfolioMapper.entityToResponseDto(any())).thenReturn(expectedResponse);

        PortfolioResponseDto result = portfolioService.updatePortfolioItem(portfolioId, "Updated Title Only", null, null);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title Only");
        verify(portfolioRepository).save(any(PortfolioItem.class));
    }

    @Test
    void updatePortfolioItem_WhenNotFound_ShouldThrowException() {
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.updatePortfolioItem(portfolioId, "title", null, null))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void updatePortfolioItem_WithNewImages_ShouldSetFirstAsPrimary() {
        String portfolioId = "portfolio-id-1";
        List<String> newImageUrls = List.of("/uploads/new-primary.jpg", "/uploads/secondary.jpg");

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenAnswer(invocation -> {
            PortfolioItem saved = invocation.getArgument(0);
            assertThat(saved.getImageUrl()).isEqualTo("/uploads/new-primary.jpg");
            assertThat(saved.getImageUrls()).hasSize(2);
            return saved;
        });

        PortfolioResponseDto resp = new PortfolioResponseDto();
        resp.setPortfolioId(portfolioId);
        when(portfolioMapper.entityToResponseDto(any())).thenReturn(resp);

        portfolioService.updatePortfolioItem(portfolioId, null, newImageUrls, null);

        verify(portfolioRepository).save(any(PortfolioItem.class));
    }

    // ============ deletePortfolioItem ============

    @Test
    void deletePortfolioItem_ShouldDeleteSuccessfully() {
        String portfolioId = "portfolio-to-delete";
        PortfolioItem itemToDelete = new PortfolioItem("Item", "/img.jpg", null, new ArrayList<>());
        itemToDelete.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(itemToDelete));

        portfolioService.deletePortfolioItem(portfolioId);

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).delete(itemToDelete);
    }

    @Test
    void deletePortfolioItem_WhenPortfolioNotFound_ShouldThrowException() {
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.deletePortfolioItem(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).delete(any());
    }

    // ============ archivePortfolioItem ============

    @Test
    void archivePortfolioItem_ShouldArchiveSuccessfully() {
        String portfolioId = "portfolio-to-archive";
        PortfolioItem item = new PortfolioItem("Item", "/img.jpg", null, new ArrayList<>());
        item.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(item));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(item);

        portfolioService.archivePortfolioItem(portfolioId);

        assertThat(item.isArchived()).isTrue();
        verify(portfolioRepository).save(item);
    }

    @Test
    void archivePortfolioItem_WhenNotFound_ShouldThrowException() {
        when(portfolioRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.archivePortfolioItem("non-existent-id"))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).save(any());
    }

    // ============ unarchivePortfolioItem ============

    @Test
    void unarchivePortfolioItem_ShouldUnarchiveSuccessfully() {
        String portfolioId = "portfolio-to-unarchive";
        PortfolioItem item = new PortfolioItem("Item", "/img.jpg", null, new ArrayList<>());
        item.setPortfolioId(portfolioId);
        item.setArchived(true);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(item));
        when(portfolioRepository.save(any(PortfolioItem.class))).thenReturn(item);

        portfolioService.unarchivePortfolioItem(portfolioId);

        assertThat(item.isArchived()).isFalse();
        verify(portfolioRepository).save(item);
    }

    @Test
    void unarchivePortfolioItem_WhenNotFound_ShouldThrowException() {
        when(portfolioRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.unarchivePortfolioItem("non-existent-id"))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).save(any());
    }

    // ============ getArchivedPortfolioItems ============

    @Test
    void getArchivedPortfolioItems_ShouldReturnAllArchivedItems() {
        PortfolioItem archivedItem = new PortfolioItem("Archived", "/img.jpg", null, new ArrayList<>());
        archivedItem.setPortfolioId("archived-id-1");
        archivedItem.setArchived(true);

        PortfolioResponseDto archivedDto = new PortfolioResponseDto();
        archivedDto.setPortfolioId("archived-id-1");
        archivedDto.setArchived(true);

        when(portfolioRepository.findByArchivedTrue()).thenReturn(List.of(archivedItem));
        when(portfolioMapper.entityToResponseDto(archivedItem)).thenReturn(archivedDto);

        List<PortfolioResponseDto> result = portfolioService.getArchivedPortfolioItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isArchived()).isTrue();
        verify(portfolioRepository).findByArchivedTrue();
    }

    @Test
    void getArchivedPortfolioItems_WhenNoneArchived_ShouldReturnEmptyList() {
        when(portfolioRepository.findByArchivedTrue()).thenReturn(List.of());

        List<PortfolioResponseDto> result = portfolioService.getArchivedPortfolioItems();

        assertThat(result).isEmpty();
        verify(portfolioRepository).findByArchivedTrue();
    }

    // ============ getPortfolioItemsByType ============

    @Test
    void getPortfolioItemsByType_WhenTypeExists_ShouldReturnMatchingItems() {
        String type = "Kitchen";
        when(portfolioRepository.findByTypeAndArchivedFalse(type)).thenReturn(List.of(portfolioItem1, portfolioItem2));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);
        when(portfolioMapper.entityToResponseDto(portfolioItem2)).thenReturn(responseDto2);

        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(type);

        assertThat(result).hasSize(2);
        verify(portfolioRepository).findByTypeAndArchivedFalse(type);
    }

    @Test
    void getPortfolioItemsByType_WhenNoMatchingType_ShouldReturnEmptyList() {
        when(portfolioRepository.findByTypeAndArchivedFalse("Exterior")).thenReturn(List.of());

        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType("Exterior");

        assertThat(result).isEmpty();
        verify(portfolioRepository).findByTypeAndArchivedFalse("Exterior");
    }
}

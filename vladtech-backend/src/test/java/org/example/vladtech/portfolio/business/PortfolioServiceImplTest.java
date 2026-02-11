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

    @Mock
    private org.example.vladtech.filestorageservice.IFileStorageService fileStorageService;

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
        assertThat(result.get(1).getTitle()).isEqualTo("Complete Kitchen Remodel");
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

    @Test
    void getAllPortfolioItems_ShouldMapAllItemsCorrectly() {
        when(portfolioRepository.findByArchivedFalse()).thenReturn(List.of(portfolioItem1, portfolioItem2));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);
        when(portfolioMapper.entityToResponseDto(portfolioItem2)).thenReturn(responseDto2);

        List<PortfolioResponseDto> result = portfolioService.getAllPortfolioItems();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PortfolioResponseDto::getTitle)
                .containsExactly("Modern Kitchen Counter", "Complete Kitchen Remodel");
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

    @Test
    void getPortfolioItemById_ShouldHandleValidId() {
        String portfolioId = "valid-portfolio-id";
        portfolioItem1.setPortfolioId(portfolioId);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolioItem1));
        when(portfolioMapper.entityToResponseDto(portfolioItem1)).thenReturn(responseDto1);

        PortfolioResponseDto result = portfolioService.getPortfolioItemById(portfolioId);

        assertThat(result).isNotNull();
        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioMapper).entityToResponseDto(portfolioItem1);
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
    void deletePortfolioItem_ShouldDeleteSuccessfully() throws Exception {
        String portfolioId = "portfolio-to-delete";
        String imageUrl = "/api/uploads/portfolio/delete-me.jpg";
        String fileId = "delete-me.jpg";
        
        PortfolioItem itemToDelete = new PortfolioItem("Item to Delete", imageUrl, null, new ArrayList<>());
        itemToDelete.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(itemToDelete));

        portfolioService.deletePortfolioItem(portfolioId);

        verify(portfolioRepository).findById(portfolioId);
        verify(fileStorageService).delete(fileId);
        verify(portfolioRepository).delete(itemToDelete);
    }

    @Test
    void deletePortfolioItem_WhenPortfolioNotFound_ShouldThrowException() throws Exception {
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.deletePortfolioItem(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).delete(any());
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    void deletePortfolioItem_WhenImageUrlIsNull_ShouldNotDeleteFile() throws Exception {
        // Arrange
        String portfolioId = "portfolio-no-image";
        PortfolioItem itemWithoutImage = new PortfolioItem(
                "Item Without Image",
                null,
                null,
                new ArrayList<>());
        itemWithoutImage.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(itemWithoutImage));

        // Act
        portfolioService.deletePortfolioItem(portfolioId);

        // Assert
        verify(portfolioRepository).findById(portfolioId);
        verify(fileStorageService, never()).delete(any());
        verify(portfolioRepository).delete(itemWithoutImage);
    }

    @Test
    void deletePortfolioItem_WhenImageUrlIsEmpty_ShouldNotDeleteFile() throws Exception {
        // Arrange
        String portfolioId = "portfolio-empty-image";
        PortfolioItem itemWithEmptyImage = new PortfolioItem(
                "Item With Empty Image",
                "",
                null,
                new ArrayList<>());
        itemWithEmptyImage.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(itemWithEmptyImage));

        // Act
        portfolioService.deletePortfolioItem(portfolioId);

        // Assert
        verify(portfolioRepository).findById(portfolioId);
        verify(fileStorageService, never()).delete(any());
        verify(portfolioRepository).delete(itemWithEmptyImage);
    }

    @Test
    void deletePortfolioItem_WhenFileDeleteFails_ShouldStillDeletePortfolio() throws Exception {
        // Arrange
        String portfolioId = "portfolio-with-file-error";
        String imageUrl = "/api/uploads/portfolio/problem-file.jpg";
        String fileId = "problem-file.jpg";
        
        PortfolioItem itemToDelete = new PortfolioItem(
                "Item With File Error",
                imageUrl,
                null,
                new ArrayList<>());
        itemToDelete.setPortfolioId(portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(itemToDelete));
        doThrow(new RuntimeException("S3 service unavailable")).when(fileStorageService).delete(fileId);

        // Act
        portfolioService.deletePortfolioItem(portfolioId);

        // Assert - Portfolio should still be deleted even if file deletion fails
        verify(portfolioRepository).findById(portfolioId);
        verify(fileStorageService).delete(fileId);
        verify(portfolioRepository).delete(itemToDelete);
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
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.archivePortfolioItem(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio item not found with id: " + portfolioId);
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
        String portfolioId = "non-existent-id";
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.unarchivePortfolioItem(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio item not found with id: " + portfolioId);
        verify(portfolioRepository, never()).save(any());
    }

    // ============ getArchivedPortfolioItems ============

    @Test
    void getArchivedPortfolioItems_ShouldReturnAllArchivedItems() {
        PortfolioItem archivedItem1 = new PortfolioItem("Archived Item 1", "/uploads/portfolio/archived1.jpg", null, new ArrayList<>());
        archivedItem1.setPortfolioId("archived-id-1");
        archivedItem1.setArchived(true);

        PortfolioItem archivedItem2 = new PortfolioItem("Archived Item 2", "/uploads/portfolio/archived2.jpg", null, new ArrayList<>());
        archivedItem2.setPortfolioId("archived-id-2");
        archivedItem2.setArchived(true);

        PortfolioResponseDto archivedDto1 = new PortfolioResponseDto();
        archivedDto1.setPortfolioId("archived-id-1");
        archivedDto1.setTitle("Archived Item 1");
        archivedDto1.setArchived(true);

        PortfolioResponseDto archivedDto2 = new PortfolioResponseDto();
        archivedDto2.setPortfolioId("archived-id-2");
        archivedDto2.setTitle("Archived Item 2");
        archivedDto2.setArchived(true);

        when(portfolioRepository.findByArchivedTrue()).thenReturn(List.of(archivedItem1, archivedItem2));
        when(portfolioMapper.entityToResponseDto(archivedItem1)).thenReturn(archivedDto1);
        when(portfolioMapper.entityToResponseDto(archivedItem2)).thenReturn(archivedDto2);

        List<PortfolioResponseDto> result = portfolioService.getArchivedPortfolioItems();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPortfolioId()).isEqualTo("archived-id-1");
        assertThat(result.get(1).getPortfolioId()).isEqualTo("archived-id-2");
        verify(portfolioRepository, times(1)).findByArchivedTrue();
        verify(portfolioMapper, times(2)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getArchivedPortfolioItems_WhenNoneArchived_ShouldReturnEmptyList() {
        when(portfolioRepository.findByArchivedTrue()).thenReturn(List.of());

        List<PortfolioResponseDto> result = portfolioService.getArchivedPortfolioItems();

        assertThat(result).isEmpty();
        verify(portfolioRepository, times(1)).findByArchivedTrue();
        verify(portfolioMapper, never()).entityToResponseDto(any());
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
        assertThat(result).containsExactly(responseDto1, responseDto2);
        verify(portfolioRepository).findByTypeAndArchivedFalse(type);
        verify(portfolioMapper, times(2)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getPortfolioItemsByType_WhenNoMatchingType_ShouldReturnEmptyList() {
        String type = "Exterior";
        when(portfolioRepository.findByTypeAndArchivedFalse(type)).thenReturn(List.of());

        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(type);

        assertThat(result).isEmpty();
        verify(portfolioRepository).findByTypeAndArchivedFalse(type);
        verify(portfolioMapper, never()).entityToResponseDto(any());
    }

    @Test
    void getPortfolioItemsByType_WithDifferentTypes_ShouldReturnCorrectItems() {
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

        when(portfolioRepository.findByTypeAndArchivedFalse(bathroomType)).thenReturn(List.of(bathroomItem));
        when(portfolioMapper.entityToResponseDto(bathroomItem)).thenReturn(bathroomDto);

        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(bathroomType);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(bathroomType);
        assertThat(result.get(0).getTitle()).isEqualTo("Luxury Bathroom");
        verify(portfolioRepository).findByTypeAndArchivedFalse(bathroomType);
    }

    @Test
    void getPortfolioItemsByType_ShouldMapAllItemsCorrectly() {
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

        when(portfolioRepository.findByTypeAndArchivedFalse(type)).thenReturn(List.of(item1, item2, item3));
        when(portfolioMapper.entityToResponseDto(item1)).thenReturn(dto1);
        when(portfolioMapper.entityToResponseDto(item2)).thenReturn(dto2);
        when(portfolioMapper.entityToResponseDto(item3)).thenReturn(dto3);

        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(type);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(PortfolioResponseDto::getTitle)
                .containsExactly("Living Room", "Bedroom", "Office");
        assertThat(result).allMatch(dto -> type.equals(dto.getType()));
        verify(portfolioRepository).findByTypeAndArchivedFalse(type);
        verify(portfolioMapper, times(3)).entityToResponseDto(any(PortfolioItem.class));
    }

    @Test
    void getPortfolioItemsByType_WithNullType_ShouldCallRepository() {
        when(portfolioRepository.findByTypeAndArchivedFalse(null)).thenReturn(List.of());

        List<PortfolioResponseDto> result = portfolioService.getPortfolioItemsByType(null);

        assertThat(result).isEmpty();
        verify(portfolioRepository).findByTypeAndArchivedFalse(null);
    }
}

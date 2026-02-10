package org.example.vladtech.portfolio.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.example.vladtech.portfolio.mapperlayer.PortfolioMapper;
import org.example.vladtech.portfolio.presentation.PortfolioCommentDto;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;
    private final org.example.vladtech.filestorageservice.IFileStorageService fileStorageService;

    @Override
    public List<PortfolioResponseDto> getAllPortfolioItems() {
        log.info("Fetching all non-archived portfolio items");
        List<PortfolioItem> portfolioItems = portfolioRepository.findByArchivedFalse();
        return portfolioItems.stream()
                .map(portfolioMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioResponseDto> getPortfolioItemsByType(String type) {
        log.info("Fetching non-archived portfolio items by type: {}", type);
        List<PortfolioItem> portfolioItems = portfolioRepository.findByTypeAndArchivedFalse(type);
        return portfolioItems.stream()
                .map(portfolioMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioResponseDto getPortfolioItemById(String portfolioId) {
        log.info("Fetching portfolio item with id: {}", portfolioId);
        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio item not found with id: " + portfolioId));
        return portfolioMapper.entityToResponseDto(portfolioItem);
    }

    @Override
    public PortfolioCommentDto addComment(String portfolioId, String commentText, String userId, String userName) {
        log.info("Adding comment to portfolio item {} by user {}", portfolioId, userName);

        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException("Portfolio item not found with id: " + portfolioId));

        // Create new comment
        PortfolioComment comment = new PortfolioComment();
        comment.setAuthorName(userName);
        comment.setAuthorUserId(userId);
        comment.setTimestamp(Instant.now());
        comment.setText(commentText);

        // Add comment to portfolio item
        portfolioItem.getComments().add(comment);

        // Save updated portfolio item
        portfolioRepository.save(portfolioItem);

        log.info("Comment added successfully to portfolio item {}", portfolioId);

        // Return the newly created comment as DTO
        return new PortfolioCommentDto(
                comment.getAuthorName(),
                comment.getAuthorUserId(),
                comment.getTimestamp(),
                comment.getText()
        );
    }

    @Override
    public PortfolioResponseDto createPortfolioItem(String title, String imageUrl, List<String> imageUrls, String type) {
        log.info("Creating new portfolio item with title: {} and type: {}", title, type);

        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setTitle(title);
        portfolioItem.setImageUrl(imageUrl);
        portfolioItem.setImageUrls(imageUrls != null && !imageUrls.isEmpty() ? imageUrls : (imageUrl != null ? List.of(imageUrl) : new java.util.ArrayList<>()));
        portfolioItem.setType(type);
        portfolioItem.setComments(new java.util.ArrayList<>());

        PortfolioItem savedItem = portfolioRepository.save(portfolioItem);
        log.info("Portfolio item created successfully with id: {}", savedItem.getPortfolioId());

        return portfolioMapper.entityToResponseDto(savedItem);
    }

    @Override
    public PortfolioResponseDto updatePortfolioItem(String portfolioId, String title, List<String> imageUrls, String type) {
        log.info("Updating portfolio item with id: {}", portfolioId);

        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException(
                        "Portfolio item not found with id: " + portfolioId));

        if (title != null && !title.isBlank()) {
            portfolioItem.setTitle(title);
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            portfolioItem.setImageUrls(imageUrls);
            portfolioItem.setImageUrl(imageUrls.get(0)); // First image is the primary
        }
        if (type != null && !type.isBlank()) {
            portfolioItem.setType(type);
        }

        PortfolioItem savedItem = portfolioRepository.save(portfolioItem);
        log.info("Portfolio item updated successfully with id: {}", savedItem.getPortfolioId());

        return portfolioMapper.entityToResponseDto(savedItem);
    }

    @Override
    public void deletePortfolioItem(String portfolioId) {
        log.info("Deleting portfolio item with id: {}", portfolioId);

        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException(
                        "Portfolio item not found with id: " + portfolioId));

        // Delete associated image file from storage (S3 or GridFS)
        if (portfolioItem.getImageUrl() != null && !portfolioItem.getImageUrl().isEmpty()) {
            try {
                String fileId = extractFileIdFromUrl(portfolioItem.getImageUrl());
                fileStorageService.delete(fileId);
                log.info("Deleted image file: {}", fileId);
            } catch (Exception e) {
                log.error("Failed to delete image file for portfolio {}: {}", portfolioId, e.getMessage());
                // Continue with portfolio deletion even if file deletion fails
            }
        }

        portfolioRepository.delete(portfolioItem);
        log.info("Portfolio item deleted successfully with id: {}", portfolioId);
    }


    private String extractFileIdFromUrl(String imageUrl) {
        // URL format: /api/uploads/portfolio/{fileId} or /api/uploads/project/{fileId}
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }

    @Override
    public void archivePortfolioItem(String portfolioId) {
        log.info("Archiving portfolio item with id: {}", portfolioId);

        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException(
                        "Portfolio item not found with id: " + portfolioId));

        portfolioItem.setArchived(true);
        portfolioRepository.save(portfolioItem);
        log.info("Portfolio item archived successfully with id: {}", portfolioId);
    }

    @Override
    public void unarchivePortfolioItem(String portfolioId) {
        log.info("Unarchiving portfolio item with id: {}", portfolioId);

        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException(
                        "Portfolio item not found with id: " + portfolioId));

        portfolioItem.setArchived(false);
        portfolioRepository.save(portfolioItem);
        log.info("Portfolio item unarchived successfully with id: {}", portfolioId);
    }

    @Override
    public List<PortfolioResponseDto> getArchivedPortfolioItems() {
        log.info("Fetching all archived portfolio items");
        List<PortfolioItem> portfolioItems = portfolioRepository.findByArchivedTrue();
        return portfolioItems.stream()
                .map(portfolioMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }
}


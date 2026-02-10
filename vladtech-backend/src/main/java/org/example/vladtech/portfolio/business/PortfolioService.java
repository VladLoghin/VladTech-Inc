package org.example.vladtech.portfolio.business;

import org.example.vladtech.portfolio.presentation.PortfolioCommentDto;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;

import java.util.List;

public interface PortfolioService {
    List<PortfolioResponseDto> getAllPortfolioItems();
    List<PortfolioResponseDto> getPortfolioItemsByType(String type);
    PortfolioResponseDto getPortfolioItemById(String portfolioId);
    PortfolioCommentDto addComment(String portfolioId, String commentText, String userId, String userName);
    PortfolioResponseDto createPortfolioItem(String title, String imageUrl, List<String> imageUrls, String type);
    PortfolioResponseDto updatePortfolioItem(String portfolioId, String title, List<String> imageUrls, String type);
    void deletePortfolioItem(String portfolioId);
    void archivePortfolioItem(String portfolioId);
    void unarchivePortfolioItem(String portfolioId);
    List<PortfolioResponseDto> getArchivedPortfolioItems();
}

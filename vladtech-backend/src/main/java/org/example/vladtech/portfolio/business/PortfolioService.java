package org.example.vladtech.portfolio.business;

import org.example.vladtech.portfolio.presentation.PortfolioCommentDto;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;

import java.util.List;

public interface PortfolioService {
    List<PortfolioResponseDto> getAllPortfolioItems();
    List<PortfolioResponseDto> getPortfolioItemsByType(String type);
    PortfolioResponseDto getPortfolioItemById(String portfolioId);
    PortfolioCommentDto addComment(String portfolioId, String commentText, String userId, String userName);
    PortfolioResponseDto createPortfolioItem(String title, String imageUrl, String type);
    void archivePortfolioItem(String portfolioId);
    void unarchivePortfolioItem(String portfolioId);
    List<PortfolioResponseDto> getArchivedPortfolioItems();
}

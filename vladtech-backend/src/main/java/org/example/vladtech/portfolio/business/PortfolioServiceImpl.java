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

    @Override
    public List<PortfolioResponseDto> getAllPortfolioItems() {
        log.info("Fetching all portfolio items");
        List<PortfolioItem> portfolioItems = portfolioRepository.findAll();
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
    public PortfolioResponseDto createPortfolioItem(String title, String imageUrl, Double rating) {
        log.info("Creating new portfolio item with title: {}", title);

        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setTitle(title);
        portfolioItem.setImageUrl(imageUrl);
        portfolioItem.setRating(rating);
        portfolioItem.setComments(new java.util.ArrayList<>());

        PortfolioItem savedItem = portfolioRepository.save(portfolioItem);
        log.info("Portfolio item created successfully with id: {}", savedItem.getPortfolioId());

        return portfolioMapper.entityToResponseDto(savedItem);
    }

    @Override
    public void deletePortfolioItem(String portfolioId) {
        log.info("Deleting portfolio item with id: {}", portfolioId);

        PortfolioItem portfolioItem = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException("Portfolio item not found with id: " + portfolioId));

        portfolioRepository.delete(portfolioItem);
        log.info("Portfolio item deleted successfully with id: {}", portfolioId);
    }
}


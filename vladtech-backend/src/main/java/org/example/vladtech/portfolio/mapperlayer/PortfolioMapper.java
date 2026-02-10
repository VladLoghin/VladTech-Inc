package org.example.vladtech.portfolio.mapperlayer;

import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.presentation.PortfolioCommentDto;
import org.example.vladtech.portfolio.presentation.PortfolioResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PortfolioMapper {

    public PortfolioResponseDto entityToResponseDto(PortfolioItem portfolioItem) {
        if (portfolioItem == null) {
            return null;
        }

        List<PortfolioCommentDto> commentDtos = portfolioItem.getComments().stream()
                .map(this::commentToDto)
                .collect(Collectors.toList());

        PortfolioResponseDto dto = new PortfolioResponseDto();
        dto.setPortfolioId(portfolioItem.getPortfolioId());
        dto.setReviewId(portfolioItem.getReviewId());
        dto.setReviewerName(portfolioItem.getReviewerName());
        dto.setTitle(portfolioItem.getTitle());
        dto.setImageUrl(portfolioItem.getImageUrl());
        dto.setImageUrls(portfolioItem.getImageUrls() != null ? portfolioItem.getImageUrls() : new java.util.ArrayList<>());
        dto.setType(portfolioItem.getType());
        dto.setArchived(portfolioItem.isArchived());
        dto.setComments(commentDtos);
        return dto;
    }

    private PortfolioCommentDto commentToDto(PortfolioComment comment) {
        if (comment == null) {
            return null;
        }

        return new PortfolioCommentDto(
                comment.getAuthorName(),
                comment.getAuthorUserId(),
                comment.getTimestamp(),
                comment.getText()
        );
    }
}


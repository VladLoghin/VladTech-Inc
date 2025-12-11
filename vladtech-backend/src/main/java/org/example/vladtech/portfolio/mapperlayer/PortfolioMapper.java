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

        return new PortfolioResponseDto(
                portfolioItem.getPortfolioId(),
                portfolioItem.getTitle(),
                portfolioItem.getImageUrl(),
                portfolioItem.getRating(),
                commentDtos
        );
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


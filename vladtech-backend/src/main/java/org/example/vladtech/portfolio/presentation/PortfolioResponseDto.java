package org.example.vladtech.portfolio.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponseDto {
    private String portfolioId;
    private String title;
    private String imageUrl;
    private Double rating;
    private List<PortfolioCommentDto> comments;
}


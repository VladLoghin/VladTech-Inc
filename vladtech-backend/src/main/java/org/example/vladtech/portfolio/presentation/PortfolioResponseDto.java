package org.example.vladtech.portfolio.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponseDto {
    private String portfolioId;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Image URL cannot be empty")
    private String imageUrl;

    @NotNull(message = "Rating cannot be null")
    private Double rating;

    private List<PortfolioCommentDto> comments;
}


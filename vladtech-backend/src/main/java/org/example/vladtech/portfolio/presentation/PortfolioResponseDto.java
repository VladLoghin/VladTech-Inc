package org.example.vladtech.portfolio.presentation;

import com.mongodb.lang.Nullable;
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

    @Nullable
    private String reviewId;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Image URL cannot be empty")
    private String imageUrl;

    @NotNull(message = "Rating cannot be null")
    private Double rating;

    @NotBlank(message = "Type cannot be empty")
    private String type; // Interior, Kitchen, Bathroom, Exterior, Garden/Landscaping

    private List<PortfolioCommentDto> comments;
}


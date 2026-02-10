package org.example.vladtech.portfolio.presentation;

import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponseDto {
    private String portfolioId;

    private String reviewId;

    @Nullable
    private String reviewerName; // Name of the reviewer if sent from a review

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @Nullable
    private String imageUrl;

    private List<String> imageUrls = new ArrayList<>(); // Multiple images support

    @NotBlank(message = "Type cannot be empty")
    private String type; // Interior, Kitchen, Bathroom, Exterior, Garden/Landscaping

    private boolean archived;

    private List<PortfolioCommentDto> comments;

    // Backward-compatible constructor (without imageUrls and archived)
    public PortfolioResponseDto(String portfolioId, String reviewId, String reviewerName,
                                String title, String imageUrl, String type,
                                List<PortfolioCommentDto> comments) {
        this.portfolioId = portfolioId;
        this.reviewId = reviewId;
        this.reviewerName = reviewerName;
        this.title = title;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrl != null ? new ArrayList<>(List.of(imageUrl)) : new ArrayList<>();
        this.type = type;
        this.comments = comments;
    }
}


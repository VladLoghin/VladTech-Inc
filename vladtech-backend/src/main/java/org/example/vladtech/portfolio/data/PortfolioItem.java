package org.example.vladtech.portfolio.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "portfolio")
public class PortfolioItem {
    @Id
    private String portfolioId;

    private String reviewId;

    private String title;
    private String imageUrl;
    private Double rating; // Star rating (e.g., 4.8)
    private String type; // Type: Interior, Kitchen, Bathroom, Exterior/Yard
    private List<PortfolioComment> comments = new ArrayList<>();

    public PortfolioItem(String title, String imageUrl, Double rating, String type, List<PortfolioComment> comments) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.type = type;
        this.comments = comments != null ? comments : new ArrayList<>();
    }
}


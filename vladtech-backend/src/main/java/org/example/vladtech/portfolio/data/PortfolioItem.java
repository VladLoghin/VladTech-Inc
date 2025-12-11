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

    private String title;
    private String imageUrl;
    private Double rating; // Star rating (e.g., 4.8)
    private List<PortfolioComment> comments = new ArrayList<>();

    public PortfolioItem(String title, String imageUrl, Double rating, List<PortfolioComment> comments) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.comments = comments != null ? comments : new ArrayList<>();
    }
}


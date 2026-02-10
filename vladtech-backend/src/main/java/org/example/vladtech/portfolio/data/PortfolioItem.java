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

    private String reviewerName; // Name of the reviewer if this item was sent from a review

    private String title;
    private String imageUrl;
    private String type; // Type: Interior, Kitchen, Bathroom, Exterior/Yard
    private List<PortfolioComment> comments = new ArrayList<>();
    private boolean archived = false;

    public PortfolioItem(String title, String imageUrl, String type, List<PortfolioComment> comments) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.type = type;
        this.comments = comments != null ? comments : new ArrayList<>();
        this.archived = false;
    }
}


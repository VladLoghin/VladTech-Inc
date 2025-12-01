package org.example.vladtech.reviews.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reviews")
public class Review {
    @Id
    private String reviewId; // MongoDB will generate a unique ObjectId if this is null

    private String clientId;
    private String appointmentId;
    private String comment;
    private Boolean visible;
    private Rating rating;
    private List<Photo> photos;

    public Review(String clientId, String appointmentId, String comment, Boolean visible, Rating rating) {
        this.clientId = clientId;
        this.appointmentId = appointmentId;
        this.comment = comment;
        this.visible = visible;
        this.rating = rating;
    }
}

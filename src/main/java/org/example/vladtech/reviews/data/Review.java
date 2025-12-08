package org.example.vladtech.reviews.data;

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
@Document(collection = "reviews")
public class Review {
    @Id
    private String reviewId;

    private String clientId;
    private String appointmentId;
    private String comment;
    private Boolean visible = true; // set to false when implementing moderation
    private Rating rating;
    private List<Photo> photos = new ArrayList<>();

    public Review(String clientId, String appointmentId, String comment, Boolean visible, Rating rating) {
        this.clientId = clientId;
        this.appointmentId = appointmentId;
        this.comment = comment;
        this.visible = visible;
        this.rating = rating;
    }

    public Review(String clientId, String appointmentId, String comment, Boolean visible, Rating rating, List<Photo> photos) {
        this.clientId = clientId;
        this.appointmentId = appointmentId;
        this.comment = comment;
        this.visible = visible;
        this.rating = rating;
        this.photos = photos != null ? photos : new ArrayList<>();
    }
}

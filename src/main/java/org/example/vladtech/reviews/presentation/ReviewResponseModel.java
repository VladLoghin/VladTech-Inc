package org.example.vladtech.reviews.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseModel {
    private String reviewId;
    private String clientId;
    private String appointmentId;
    private String comment;
    private Boolean visible;
    private Rating rating;
    private List<Photo> photos;
}

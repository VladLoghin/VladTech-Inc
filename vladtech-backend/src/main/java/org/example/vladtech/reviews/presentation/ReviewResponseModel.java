package org.example.vladtech.reviews.presentation;

import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private String reviewId;
    @NotNull
    private String clientId;
    @NotNull
    private String appointmentId;
    @NotNull
    private String clientName;
    @NotNull
    private String comment;
    @NotNull
    private Boolean visible;
    @NotNull
    private Rating rating;
    private List<Photo> photos;
}

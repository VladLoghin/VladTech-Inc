package org.example.vladtech.reviews.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestModel {
    @NotNull
    private String clientId;
    @NotNull
    private String appointmentId;
    @NotNull
    private String comment;
    @NotNull
    private Boolean visible = true; // set to false when implementing moderation
    @NotNull
    private Rating rating;
    private List<Photo> photos;
}

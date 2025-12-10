package org.example.vladtech.reviews.mapperlayer;

import javax.annotation.processing.Generated;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T09:14:25-0500",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 17.0.17 (Microsoft)"
)
@Component
public class ReviewRequestMapperImpl implements ReviewRequestMapper {

    @Override
    public Review requestModelToEntity(ReviewRequestModel reviewRequestModel) {
        if ( reviewRequestModel == null ) {
            return null;
        }

        Review review = new Review();

        review.setClientId( reviewRequestModel.getClientId() );
        review.setAppointmentId( reviewRequestModel.getAppointmentId() );
        review.setComment( reviewRequestModel.getComment() );
        review.setVisible( reviewRequestModel.getVisible() );
        review.setRating( reviewRequestModel.getRating() );

        return review;
    }
}

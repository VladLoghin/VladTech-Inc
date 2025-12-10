package org.example.vladtech.reviews.mapperlayer;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T09:14:25-0500",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 17.0.17 (Microsoft)"
)
@Component
public class ReviewResponseMapperImpl implements ReviewResponseMapper {

    @Override
    public ReviewResponseModel entityToResponseModel(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewResponseModel reviewResponseModel = new ReviewResponseModel();

        reviewResponseModel.setClientId( review.getClientId() );
        reviewResponseModel.setAppointmentId( review.getAppointmentId() );
        reviewResponseModel.setComment( review.getComment() );
        reviewResponseModel.setVisible( review.getVisible() );
        reviewResponseModel.setRating( review.getRating() );
        List<Photo> list = review.getPhotos();
        if ( list != null ) {
            reviewResponseModel.setPhotos( new ArrayList<Photo>( list ) );
        }

        reviewResponseModel.setReviewId( review.getReviewId() );

        return reviewResponseModel;
    }

    @Override
    public List<ReviewResponseModel> entityListToResponseModelList(List<Review> reviews) {
        if ( reviews == null ) {
            return null;
        }

        List<ReviewResponseModel> list = new ArrayList<ReviewResponseModel>( reviews.size() );
        for ( Review review : reviews ) {
            list.add( entityToResponseModel( review ) );
        }

        return list;
    }
}

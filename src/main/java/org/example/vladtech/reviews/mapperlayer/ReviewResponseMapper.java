package org.example.vladtech.reviews.mapperlayer;

import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewResponseMapper {

    // Map from entity → response DTO
    @Mapping(expression = "java(review.getReviewId())", target = "reviewId")
    ReviewResponseModel entityToResponseModel(Review review);

    List<ReviewResponseModel> entityListToResponseModelList(List<Review> reviews);
}
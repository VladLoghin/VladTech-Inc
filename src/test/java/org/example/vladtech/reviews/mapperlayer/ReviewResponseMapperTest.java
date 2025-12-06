package org.example.vladtech.reviews.mapperlayer;

import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewResponseMapperTest {

    private final ReviewResponseMapper mapper = Mappers.getMapper(ReviewResponseMapper.class);

    @Test
    void entityToResponseModel_mapsFields() {
        Review review = new Review("clientX", "apptX", "fine", true, Rating.THREE);
        review.setReviewId("rev123");

        ReviewResponseModel resp = mapper.entityToResponseModel(review);

        assertThat(resp).isNotNull();
        assertThat(resp.getReviewId()).isEqualTo("rev123");
        assertThat(resp.getClientId()).isEqualTo("clientX");
        assertThat(resp.getAppointmentId()).isEqualTo("apptX");
        assertThat(resp.getComment()).isEqualTo("fine");
        assertThat(resp.getVisible()).isTrue();
        assertThat(resp.getRating()).isEqualTo(Rating.THREE);
    }

    @Test
    void entityListToResponseModelList_mapsList() {
        Review a = new Review("c1", "a1", "c1", true, Rating.FIVE); a.setReviewId("r1");
        Review b = new Review("c2", "a2", "c2", true, Rating.TWO); b.setReviewId("r2");

        List<Review> reviews = Arrays.asList(a, b);
        List<org.example.vladtech.reviews.presentation.ReviewResponseModel> resps = mapper.entityListToResponseModelList(reviews);

        assertThat(resps).hasSize(2);
        assertThat(resps.get(0).getReviewId()).isEqualTo("r1");
        assertThat(resps.get(1).getReviewId()).isEqualTo("r2");
    }
}

package org.example.vladtech.reviews.mapperlayer;

import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.example.vladtech.reviews.data.Rating;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewRequestMapperTest {

    private final ReviewRequestMapper mapper = Mappers.getMapper(ReviewRequestMapper.class);

    @Test
    void requestModelToEntity_mapsFields_and_ignoresId() {
        ReviewRequestModel request = new ReviewRequestModel(
                "clientA",
                "appointmentA",
                "nice job",
                true,
                Rating.FOUR,
                null
        );

        Review entity = mapper.requestModelToEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getClientId()).isEqualTo("clientA");
        assertThat(entity.getAppointmentId()).isEqualTo("appointmentA");
        assertThat(entity.getComment()).isEqualTo("nice job");
        assertThat(entity.getVisible()).isTrue();
        assertThat(entity.getRating()).isEqualTo(Rating.FOUR);
        // reviewId should be left null because mapping ignores it
        assertThat(entity.getReviewId()).isNull();
    }
}

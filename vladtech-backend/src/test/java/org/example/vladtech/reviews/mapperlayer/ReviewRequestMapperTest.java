package org.example.vladtech.reviews.mapperlayer;

import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ReviewRequestMapperTest {

    private final ReviewRequestMapper mapper = Mappers.getMapper(ReviewRequestMapper.class);

    @Test
    void requestModelToEntity_mapsFields_and_ignoresId() {
        // Create a ReviewRequestModel with all fields
        ReviewRequestModel request = new ReviewRequestModel(
                "clientA",
                "appointmentA",
                "nice job",
                true,
                Rating.FOUR
        );

        // Map to entity
        Review entity = mapper.requestModelToEntity(request);

        // Assert all fields are correctly mapped
        assertNotNull(entity, "Mapped entity should not be null");
        assertEquals("clientA", entity.getClientId(), "ClientId should match");
        assertEquals("appointmentA", entity.getAppointmentId(), "AppointmentId should match");
        assertEquals("nice job", entity.getComment(), "Comment should match");
        assertTrue(entity.getVisible(), "Visible should be true");
        assertEquals(Rating.FOUR, entity.getRating(), "Rating should match");
        assertNull(entity.getReviewId(), "reviewId should be null because it is ignored by mapper");
    }
}

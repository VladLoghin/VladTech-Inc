package org.example.vladtech.reviews.mapperlayer;

import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.presentation.ReviewResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewResponseMapperTest {

    private final ReviewResponseMapper mapper = Mappers.getMapper(ReviewResponseMapper.class);

    @Test
    void entityToResponseModel_mapsFields() {
        Review review = new Review("clientX", "apptX", "fine", true, Rating.THREE);
        review.setReviewId("rev123");

        ReviewResponseModel resp = mapper.entityToResponseModel(review);

        assertNotNull(resp);
        assertEquals("rev123", resp.getReviewId());
        assertEquals("clientX", resp.getClientId());
        assertEquals("apptX", resp.getAppointmentId());
        assertEquals("fine", resp.getComment());
        assertTrue(resp.getVisible());
        assertEquals(Rating.THREE, resp.getRating());
    }

    @Test
    void entityListToResponseModelList_mapsList() {
        Review a = new Review("c1", "a1", "c1", true, Rating.FIVE);
        a.setReviewId("r1");
        Review b = new Review("c2", "a2", "c2", true, Rating.TWO);
        b.setReviewId("r2");

        List<Review> reviews = Arrays.asList(a, b);
        List<ReviewResponseModel> resps = mapper.entityListToResponseModelList(reviews);

        assertEquals(2, resps.size());
        assertEquals("r1", resps.get(0).getReviewId());
        assertEquals("r2", resps.get(1).getReviewId());
    }
}

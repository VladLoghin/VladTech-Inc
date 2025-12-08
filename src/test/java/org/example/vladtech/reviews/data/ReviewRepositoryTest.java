package org.example.vladtech.reviews.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanup() {
        reviewRepository.deleteAll();
    }

//    @Test
//    void findByVisibleTrue_returnsOnlyVisibleReviews() {
//        Review visible = new Review("c1", "a1", "visible comment", true, Rating.FIVE);
//        Review notVisible = new Review("c2", "a2", "hidden comment", false, Rating.ONE);
//
//        reviewRepository.save(visible);
//        reviewRepository.save(notVisible);
//
//        List<Review> visibleList = reviewRepository.findByVisibleTrue();
//
//        assertEquals(1, visibleList.size());
//        assertEquals("visible comment", visibleList.get(0).getComment());
//        assertTrue(visibleList.get(0).getVisible());
//    }
}

package org.example.vladtech.reviews.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanup() {
        reviewRepository.deleteAll();
    }

    @Test
    void findByVisibleTrue_returnsOnlyVisibleReviews() {
        Review visible = new Review("c1", "a1", "visible comment", true, Rating.FIVE);
        Review notVisible = new Review("c2", "a2", "hidden comment", false, Rating.ONE);

        reviewRepository.save(visible);
        reviewRepository.save(notVisible);

        List<Review> visibleList = reviewRepository.findByVisibleTrue();

        assertThat(visibleList).hasSize(1);
        assertThat(visibleList.get(0).getComment()).isEqualTo("visible comment");
        assertThat(visibleList.get(0).getVisible()).isTrue();
    }
}

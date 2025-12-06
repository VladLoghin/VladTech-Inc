package org.example.vladtech.reviews.presentation;

import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    private Review review1;
    private Review review2;

    @BeforeEach
    void setup() {
        // Clear the repository before each test
        reviewRepository.deleteAll();

        // Sample reviews
        review1 = new Review("client1", "appt1", "Great service", true, Rating.FIVE, List.of());
        review2 = new Review("client2", "appt2", "Okay experience", true, Rating.THREE,
                List.of(new Photo("client2", "photo.jpg", "image/jpeg", new byte[0]))
                );

        // Save sample reviews
        reviewRepository.saveAll(Arrays.asList(review1, review2));
    }

//    @Test
//    void getAllVisibleReviews_returnsOkAndJsonArray() throws Exception {
//        mockMvc.perform(get("/api/reviews/visible")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[?(@.clientId == 'client1')]").exists())
//                .andExpect(jsonPath("$[?(@.clientId == 'client2')]").exists());
//    }
}

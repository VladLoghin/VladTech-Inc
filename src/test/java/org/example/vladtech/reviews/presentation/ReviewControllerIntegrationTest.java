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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository; // real repository

    @BeforeEach
    void setup() {
        // Clean the database before each test
        reviewRepository.deleteAll();

        Review review1 = new Review("client1", "appt1", "Great service", true, Rating.FIVE, List.of());
        Review review2 = new Review(
                "client2",
                "appt2",
                "Okay experience",
                true,
                Rating.THREE,
                List.of(new Photo("client2", "photo.jpg", "image/jpeg", "/uploads/reviews/photo.jpg"))
        );

        reviewRepository.saveAll(List.of(review1, review2));
    }

    @Test
    void getAllVisibleReviews_returnsOkAndJsonArray() throws Exception {
        mockMvc.perform(get("/api/reviews/visible")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.clientId == 'client1')]").exists())
                .andExpect(jsonPath("$[?(@.clientId == 'client2')]").exists());
    }

    @Test
    @WithMockUser(username = "client3", authorities = {"Client"})
    void createReview_savesAndReturnsReview() throws Exception {
        String reviewJson = """
            {
                "clientId": "client3",
                "appointmentId": "appt3",
                "comment": "Excellent!",
                "visible": true,
                "rating": "FIVE"
            }
            """;

        MockMultipartFile reviewPart = new MockMultipartFile(
                "review",
                "review.json",
                "application/json",
                reviewJson.getBytes()
        );

        MockMultipartFile photosPart = new MockMultipartFile(
                "photos",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/reviews")
                        .file(reviewPart)
                        .file(photosPart)
                        .with(request -> { request.setMethod("POST"); return request; })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client3"))
                .andExpect(jsonPath("$.appointmentId").value("appt3"))
                .andExpect(jsonPath("$.comment").value("Excellent!"))
                .andExpect(jsonPath("$.visible").value(true))
                .andExpect(jsonPath("$.rating").value("FIVE"));
    }
}

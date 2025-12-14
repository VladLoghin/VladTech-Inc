package org.example.vladtech.reviews.presentation;

import org.example.vladtech.reviews.business.ReviewService;
import org.example.vladtech.reviews.business.ReviewServiceImpl;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository; // real repository

    @Autowired
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewService reviewServiceMock;

    @BeforeEach
    void setup() {
        // Clean the database before each test
        reviewRepository.deleteAll();

        Review review1 = new Review("client1", "appt1", "Great service", "hell yeah!",true, Rating.FIVE, List.of());
        Review review2 = new Review(
                "client2",
                "appt2",
                "John",
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
        // Mock the Jwt object
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn("client3");
        when(jwt.getClaim("scope")).thenReturn("review:write");

        // Inject the mocked Jwt into the security context with the required authority
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, Collections.singletonList(() -> "Client"))
        );

        String reviewJson = """
        {
            "clientId": "client3",
            "appointmentId": "appt3",
            "clientName": "Alice",
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
                .andExpect(jsonPath("$.clientName").value("Alice"))
                .andExpect(jsonPath("$.comment").value("Excellent!"))
                .andExpect(jsonPath("$.visible").value(false))
                .andExpect(jsonPath("$.rating").value("FIVE"));
    }

}

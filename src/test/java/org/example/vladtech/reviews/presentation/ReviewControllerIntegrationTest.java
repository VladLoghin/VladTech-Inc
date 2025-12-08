package org.example.vladtech.reviews.presentation;

import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.data.ReviewRepository;
import org.example.vladtech.reviews.business.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class)
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    private Review review1;
    private Review review2;

    @BeforeEach
    void setup() {
        review1 = new Review("client1", "appt1", "Great service", true, Rating.FIVE, List.of());
        review2 = new Review(
                "client2",
                "appt2",
                "Okay experience",
                true,
                Rating.THREE,
                List.of(new Photo("client2", "photo.jpg", "image/jpeg", "/uploads/reviews/photo.jpg"))
        );

        // Mock the service methods that controller calls
        when(reviewService.getAllVisibleReviews()).thenReturn((List<Review>) Arrays.asList(review1, review2));
        when(reviewService.createReview(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
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
//
//    @Test
//    @WithMockUser(username = "client3", authorities = {"Client"})
//    void createReview_savesAndReturnsReview() throws Exception {
//        String reviewJson = """
//            {
//                "clientId": "client3",
//                "appointmentId": "appt3",
//                "comment": "Excellent!",
//                "visible": true,
//                "rating": "FIVE"
//            }
//            """;
//
//        MockMultipartFile reviewPart = new MockMultipartFile(
//                "review",
//                "review.json",
//                "application/json",
//                reviewJson.getBytes()
//        );
//
//        MockMultipartFile photosPart = new MockMultipartFile(
//                "photos",
//                new byte[0]
//        );
//
//        mockMvc.perform(multipart("/api/reviews")
//                        .file(reviewPart)
//                        .file(photosPart)
//                        .with(request -> { request.setMethod("POST"); return request; })
//                )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.clientId").value("client3"))
//                .andExpect(jsonPath("$.appointmentId").value("appt3"))
//                .andExpect(jsonPath("$.comment").value("Excellent!"))
//                .andExpect(jsonPath("$.visible").value(true))
//                .andExpect(jsonPath("$.rating").value("FIVE"));
//
//        verify(reviewService, times(1)).createReview(any(), any());
//    }
}

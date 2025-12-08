package org.example.vladtech.reviews.presentation;

import org.example.vladtech.reviews.business.ReviewService;
import org.example.vladtech.reviews.data.Rating;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService; // replaces deprecated @MockBean

    @Test
    void getAllVisibleReviews_returnsOnlyVisibleReviews() throws Exception {
        ReviewResponseModel review1 = new ReviewResponseModel("c1", "a1", "Great!", true, Rating.FIVE);
        ReviewResponseModel review2 = new ReviewResponseModel("c2", "a2", "Good", true, Rating.FOUR);

        when(reviewService.getAllVisibleReviews()).thenReturn(List.of(review1, review2));

        mockMvc.perform(get("/api/reviews/visible").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.clientId == 'c1')]").exists())
                .andExpect(jsonPath("$[?(@.clientId == 'c2')]").exists());

        verify(reviewService, times(1)).getAllVisibleReviews();
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

        MockMultipartFile reviewPart = new MockMultipartFile("review", "review.json", "application/json", reviewJson.getBytes());
        MockMultipartFile photosPart = new MockMultipartFile("photos", new byte[0]);

        ReviewResponseModel savedReview = new ReviewResponseModel("client3", "appt3", "Excellent!", true, Rating.FIVE);

        when(reviewService.createReview(any(ReviewRequestModel.class), any(MultipartFile[].class)))
                .thenReturn(savedReview);

        mockMvc.perform(multipart("/api/reviews")
                        .file(reviewPart)
                        .file(photosPart)
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client3"))
                .andExpect(jsonPath("$.appointmentId").value("appt3"))
                .andExpect(jsonPath("$.comment").value("Excellent!"))
                .andExpect(jsonPath("$.visible").value(true))
                .andExpect(jsonPath("$.rating").value("FIVE"));

        verify(reviewService, times(1)).createReview(any(ReviewRequestModel.class), any(MultipartFile[].class));
    }
}
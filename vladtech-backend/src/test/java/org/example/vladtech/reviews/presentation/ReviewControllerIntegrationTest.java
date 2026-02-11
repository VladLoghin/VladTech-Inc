package org.example.vladtech.reviews.presentation;

import org.example.vladtech.auth.dataaccess.UserProfileRepository;
import org.example.vladtech.contact.businesslayer.ContactServiceImpl;
import org.example.vladtech.reviews.business.BadWordFilterService;
import org.example.vladtech.reviews.business.ReviewService;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import org.springframework.mock.web.MockMultipartFile;


import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc
@Import(ReviewControllerIntegrationTest.TestSecurityConfig.class)
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactServiceImpl contactService;
    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserProfileRepository userProfileRepository;

    @MockitoBean
    private BadWordFilterService badWordFilterService;

    private ReviewResponseModel r1;
    private ReviewResponseModel r2;

    @BeforeEach
    void setup() {
        r1 = new ReviewResponseModel(
                "review-1",
                "PID",

                "client1",
                "appt1",
                "Roger",
                "hell yeah!",
                true,
                Rating.FIVE,
                List.of(),
                "Interior"
        );

        r2 = new ReviewResponseModel(
                "review-2",
                "PID",
                "client2",
                "appt2",
                "John",
                "Okay experience",
                true,
                Rating.THREE,
                List.of(new Photo("client2", "photo.jpg", "image/jpeg", "/uploads/reviews/photo.jpg")),
                "Interior"
        );

        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(List.of(r1, r2));
    }

    @Test
    void getAllVisibleReviews_returnsOkAndJsonArray() throws Exception {
        mockMvc.perform(get("/api/reviews/visible")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.clientId == 'client1')]").exists())
                .andExpect(jsonPath("$[?(@.clientId == 'client2')]").exists());
    }

    @Test
    void getAllReviews_asClient_forbidden() throws Exception {
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllReviews_asAdmin_ok() throws Exception {
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllReviews_asEmployee_ok() throws Exception{
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Employee")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllReviews_withClientNameAndRating_returnsFilteredReviews() throws Exception {
        // Arrange
        when(reviewService.getAllReviews("client1", Rating.FIVE, null, null)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .param("clientName", "client1")
                        .param("rating", "FIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value("FIVE"));
    }

    @Test
    void getAllReviews_withClientNameOnly_returnsFilteredReviews() throws Exception {
        // Arrange
        when(reviewService.getAllReviews("client1", null, null, null)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .param("clientName", "client1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientId").value("client1"));
    }

    @Test
    void getAllReviews_withRatingOnly_returnsFilteredReviews() throws Exception {
        // Arrange
        when(reviewService.getAllReviews(null, Rating.FIVE, null, null)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .param("rating", "FIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value("FIVE"));
    }

    @Test
    void getAllReviews_withoutParameters_returnsAllReviews() throws Exception {
        // Arrange
        when(reviewService.getAllReviews(null, null, null, null)).thenReturn(List.of(r1, r2));

        // Act & Assert
        mockMvc.perform(get("/api/reviews")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.clientId == 'client1')]").exists())
                .andExpect(jsonPath("$[?(@.clientId == 'client2')]").exists());
    }

    @Test
    void getAllVisibleReviews_withClientNameAndRating_returnsFilteredReviews() throws Exception {
        // Arrange
        when(reviewService.getAllVisibleReviews("client1", Rating.FIVE, null, null)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/visible")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .param("clientName", "client1")
                        .param("rating", "FIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientId").value("client1"));
    }

    @Test
    void getAllVisibleReviews_withClientNameOnly_returnsFilteredReviews() throws Exception {
        // Arrange
        when(reviewService.getAllVisibleReviews("client1", null, null, null)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/visible")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .param("clientName", "client1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clientId").value("client1"));
    }

    @Test
    void getAllVisibleReviews_withRatingOnly_returnsFilteredReviews() throws Exception {
        // Arrange
        when(reviewService.getAllVisibleReviews(null, Rating.FIVE, null, null)).thenReturn(List.of(r1));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/visible")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .param("rating", "FIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value("FIVE"));
    }

    @Test
    void getAllVisibleReviews_withoutParameters_returnsAllVisibleReviews() throws Exception {
        // Arrange
        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(List.of(r1, r2));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/visible")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.clientId == 'client1')]").exists())
                .andExpect(jsonPath("$[?(@.clientId == 'client2')]").exists());
    }

    @Test
    void patchReviewVisibility_asAdmin_ok() throws Exception {
        // Arrange
        ReviewResponseModel updated = new ReviewResponseModel(
                "review-1",
                "PID",

                "client1",
                "appt1",
                "Roger",
                "hell yeah!",
                false,
                Rating.FIVE,
                List.of(),
                "Interior"
        );

        when(reviewService.updateReviewVisibility("review-1", false)).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(patch("/api/reviews/review-1/visibility")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visible\":false}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reviewId").value("review-1"))
                .andExpect(jsonPath("$.visible").value(false));

        verify(reviewService).updateReviewVisibility("review-1", false);
    }

    @Test
    void patchReviewVisibility_asClient_forbidden() throws Exception {
        mockMvc.perform(patch("/api/reviews/review-1/visibility")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visible\":true}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(reviewService, never()).updateReviewVisibility(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void patchReviewVisibility_missingBody_returnsBadRequest() throws Exception {
        // Because @RequestBody is required by default, Spring will reject missing body with 400
        mockMvc.perform(patch("/api/reviews/review-1/visibility")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).updateReviewVisibility(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void patchReviewVisibility_visibleNull_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/reviews/review-1/visibility")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visible\":null}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).updateReviewVisibility(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void patchReviewVisibility_blankReviewId_returnsBadRequest() throws Exception {
        // "%20" becomes " " which isBlank() == true in your controller
        mockMvc.perform(patch("/api/reviews/%20/visibility")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visible\":true}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).updateReviewVisibility(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void deleteReview_asClient_noContent() throws Exception {
        Jwt jwtWithSub = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "test-sub")
                .build();

        mockMvc.perform(delete("/api/reviews/review-1")
                        .with(jwt().jwt(jwtWithSub).authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReviewAsClient("review-1", "test-sub");
    }


    @Test
    void deleteReview_asAdmin_forbidden() throws Exception {
        mockMvc.perform(delete("/api/reviews/review-1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(reviewService, never()).deleteReviewAsClient(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void deleteReview_blankReviewId_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/reviews/%20")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).deleteReviewAsClient(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void deleteReview_missingSubClaim_returns401() throws Exception {
        Jwt noSubJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("some", "thing")
                .build();

        mockMvc.perform(delete("/api/reviews/review-1")
                        .with(jwt().jwt(noSubJwt).authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(reviewService, never()).deleteReviewAsClient(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getSatisfactionPercentage_whenVisibleEmpty_returnsOkWithZero() throws Exception {
        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/satisfaction-percentage")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
    }


    @Test
    void getSatisfactionPercentage_whenVisibleNotEmpty_returnsOkWithValue() throws Exception {
        when(reviewService.getAllVisibleReviews(null, null, null, null)).thenReturn(List.of(r1));
        when(reviewService.computeSatisfactionPercentage()).thenReturn(75.0);

        mockMvc.perform(get("/api/reviews/satisfaction-percentage")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("75.0"));

        verify(reviewService).computeSatisfactionPercentage();
    }



    @Test
    void createReview_withBadWords_returns422() throws Exception {
        Jwt jwtWithSub = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "test-sub")
                .build();

        String reviewJson = """
                {
                    "projectId": "project123",
                    "clientId": "test-sub",
                    "appointmentId": "appt123",
                    "clientName": "John Doe",
                    "comment": "This is shit service",
                    "visible": false,
                    "rating": "FIVE",
                    "sentToPortfolio": false,
                    "type": "Interior"
                }
                """;

        MockMultipartFile reviewPart = new MockMultipartFile(
                "review", "", "application/json", reviewJson.getBytes()
        );

        when(reviewService.createReview(any(), any(), eq("test-sub")))
                .thenThrow(new IllegalArgumentException("Your review contains inappropriate language. Please remove any profanity."));

        mockMvc.perform(multipart("/api/reviews")
                        .file(reviewPart)
                        .with(jwt().jwt(jwtWithSub).authorities(new SimpleGrantedAuthority("Client")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Your review contains inappropriate language. Please remove any profanity."));
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            return http.build();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-sub")
                    .build();
        }
    }
}

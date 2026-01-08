package org.example.vladtech.reviews.presentation;

import org.example.vladtech.reviews.business.ReviewService;
import org.example.vladtech.reviews.data.Photo;
import org.example.vladtech.reviews.data.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private ReviewService reviewService;

    private ReviewResponseModel r1;
    private ReviewResponseModel r2;

    @BeforeEach
    void setup() {
        r1 = new ReviewResponseModel(
                "review-1",
                "client1",
                "appt1",
                "Roger",
                "hell yeah!",
                true,
                Rating.FIVE,
                List.of()
        );

        r2 = new ReviewResponseModel(
                "review-2",
                "client2",
                "appt2",
                "John",
                "Okay experience",
                true,
                Rating.THREE,
                List.of(new Photo("client2", "photo.jpg", "image/jpeg", "/uploads/reviews/photo.jpg"))
        );

        when(reviewService.getAllVisibleReviews()).thenReturn(List.of(r1, r2));
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

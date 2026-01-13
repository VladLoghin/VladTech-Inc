package org.example.vladtech.portfolio.presentation;

import org.example.vladtech.filestorageservice.FileStorageService;
import org.example.vladtech.portfolio.business.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc
@Import(PortfolioControllerIntegrationTest.TestSecurityConfig.class)
class PortfolioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private FileStorageService fileStorageService;


    private PortfolioResponseDto item1;
    private PortfolioResponseDto item2;
    private PortfolioResponseDto item3;

    @BeforeEach
    void setup() {
        Instant now = Instant.now();

        item1 = new PortfolioResponseDto(
                "portfolio-1",
                "Modern Kitchen Counter",
                "/uploads/portfolio/kitchencounter.jpg",
                4.9,
                List.of(
                        new PortfolioCommentDto("Sarah M.", "auth0|user1", now.minusSeconds(10800), "Beautiful countertop!"),
                        new PortfolioCommentDto("John D.", "auth0|user2", now.minusSeconds(3600), "Love the modern design.")
                )
        );

        item2 = new PortfolioResponseDto(
                "portfolio-2",
                "Complete Kitchen Remodel",
                "/uploads/portfolio/kitchenremodel.jpg",
                5.0,
                List.of(
                        new PortfolioCommentDto("Emma L.", "auth0|user3", now.minusSeconds(18000), "Amazing transformation!")
                )
        );

        item3 = new PortfolioResponseDto(
                "portfolio-3",
                "Luxury Bathroom Renovation",
                "/uploads/portfolio/newbathroom.jpg",
                4.8,
                List.of(
                        new PortfolioCommentDto("Lisa K.", "auth0|user4", now.minusSeconds(14400), "Stunning bathroom design.")
                )
        );

        when(portfolioService.getAllPortfolioItems()).thenReturn(List.of(item1, item2, item3));
        when(portfolioService.getPortfolioItemById("portfolio-1")).thenReturn(item1);
        when(portfolioService.getPortfolioItemById("portfolio-2")).thenReturn(item2);
        when(portfolioService.getPortfolioItemById("portfolio-3")).thenReturn(item3);
    }

    @Test
    void getAllPortfolioItems_ShouldReturnAllItems() throws Exception {
        mockMvc.perform(get("/api/portfolio").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title",
                        containsInAnyOrder("Modern Kitchen Counter", "Complete Kitchen Remodel", "Luxury Bathroom Renovation")))
                .andExpect(jsonPath("$[*].imageUrl",
                        containsInAnyOrder(
                                "/uploads/portfolio/kitchencounter.jpg",
                                "/uploads/portfolio/kitchenremodel.jpg",
                                "/uploads/portfolio/newbathroom.jpg"
                        )))
                .andExpect(jsonPath("$[*].rating",
                        containsInAnyOrder(4.9, 5.0, 4.8)));
    }

    @Test
    void getPortfolioItemById_WhenValidId_ShouldReturnItem() throws Exception {
        mockMvc.perform(get("/api/portfolio/{portfolioId}", "portfolio-1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId", is("portfolio-1")))
                .andExpect(jsonPath("$.title", is("Modern Kitchen Counter")))
                .andExpect(jsonPath("$.imageUrl", is("/uploads/portfolio/kitchencounter.jpg")))
                .andExpect(jsonPath("$.rating", is(4.9)))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[*].authorName", containsInAnyOrder("Sarah M.", "John D.")))
                .andExpect(jsonPath("$.comments[*].text",
                        containsInAnyOrder("Beautiful countertop!", "Love the modern design.")));
    }

    @Test
    void getAllPortfolioItems_ShouldIncludeAllComments() throws Exception {
        mockMvc.perform(get("/api/portfolio").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.portfolioId=='portfolio-1')].comments[*].authorName",
                        hasItems("Sarah M.", "John D.")));
    }


    @Test
    void getPortfolioItemById_ShouldReturnCorrectRating() throws Exception {
        mockMvc.perform(get("/api/portfolio/{portfolioId}", "portfolio-2").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(5.0)))
                .andExpect(jsonPath("$.title", is("Complete Kitchen Remodel")));
    }

    @Test
    void getAllPortfolioItems_ShouldReturnItemsWithCorrectImageUrls() throws Exception {
        mockMvc.perform(get("/api/portfolio").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].imageUrl",
                        containsInAnyOrder(
                                "/uploads/portfolio/kitchencounter.jpg",
                                "/uploads/portfolio/kitchenremodel.jpg",
                                "/uploads/portfolio/newbathroom.jpg"
                        )));
    }

    @Test
    void getPortfolioItemById_WithNoComments_ShouldReturnEmptyCommentsList() throws Exception {
        PortfolioResponseDto noComments = new PortfolioResponseDto(
                "portfolio-99",
                "Simple Office",
                "/uploads/portfolio/newoffice.jpg",
                4.5,
                List.of()
        );

        when(portfolioService.getPortfolioItemById("portfolio-99")).thenReturn(noComments);

        mockMvc.perform(get("/api/portfolio/{portfolioId}", "portfolio-99").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(0)))
                .andExpect(jsonPath("$.title", is("Simple Office")))
                .andExpect(jsonPath("$.rating", is(4.5)));
    }

    /**
     * Minimal security so the MVC slice can run if your app uses resource-server JWT.
     * If your GET endpoints are public (permitAll), you can delete this and the .with(jwt()).
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
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

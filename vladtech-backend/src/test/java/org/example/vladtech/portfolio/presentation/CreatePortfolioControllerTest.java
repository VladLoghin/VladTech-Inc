package org.example.vladtech.portfolio.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class CreatePortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        portfolioRepository.deleteAll();
    }

    @Test
    void createPortfolio_AsAdmin_ShouldCreateSuccessfully() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("New Kitchen Renovation");
        request.setImageUrl("/uploads/portfolio/kitchen.jpg");
        request.setRating(4.8);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.portfolioId").exists())
                .andExpect(jsonPath("$.title", is("New Kitchen Renovation")))
                .andExpect(jsonPath("$.imageUrl", is("/uploads/portfolio/kitchen.jpg")))
                .andExpect(jsonPath("$.rating", is(4.8)))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    void createPortfolio_AsClient_ShouldBeForbidden() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("This should fail");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setRating(5.0);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "Client User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPortfolio_AsEmployee_ShouldBeForbidden() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("This should fail");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setRating(5.0);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|employee123")
                                        .claim("name", "Employee User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Employee")))
                                .authorities(new SimpleGrantedAuthority("Employee")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPortfolio_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("This should fail");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setRating(5.0);

        mockMvc.perform(post("/api/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPortfolio_WithEmptyTitle_ShouldReturnBadRequest() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setRating(5.0);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPortfolio_WithEmptyImageUrl_ShouldReturnBadRequest() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("Valid Title");
        request.setImageUrl("");
        request.setRating(5.0);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPortfolio_WithNullRating_ShouldReturnBadRequest() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("Valid Title");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setRating(null);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPortfolio_WithValidData_ShouldReturnPortfolioWithId() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("Bathroom Remodel");
        request.setImageUrl("/uploads/portfolio/bathroom.jpg");
        request.setRating(4.5);

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.portfolioId").isNotEmpty())
                .andExpect(jsonPath("$.title", is("Bathroom Remodel")))
                .andExpect(jsonPath("$.imageUrl", is("/uploads/portfolio/bathroom.jpg")))
                .andExpect(jsonPath("$.rating", is(4.5)));
    }
}

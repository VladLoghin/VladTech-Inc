package org.example.vladtech.portfolio.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.portfolio.data.PortfolioComment;
import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class PortfolioCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PortfolioItem testPortfolioItem;

    @BeforeEach
    void setup() {
        portfolioRepository.deleteAll();

        testPortfolioItem = new PortfolioItem(
                "Test Portfolio Item",
                "/uploads/portfolio/test.jpg",
                4.5,
                new ArrayList<>()
        );
        portfolioRepository.save(testPortfolioItem);
    }

    @Test
    void addComment_AsClient_ShouldAddCommentSuccessfully() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("Great work on this project!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "John Client")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName", is("John Client")))
                .andExpect(jsonPath("$.authorUserId", is("auth0|client123")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.text", is("Great work on this project!")));
    }

    @Test
    void addComment_AsAdmin_ShouldAddCommentSuccessfully() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("Admin feedback here!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName", is("Admin User")))
                .andExpect(jsonPath("$.text", is("Admin feedback here!")));
    }

    @Test
    void addComment_AsEmployee_ShouldBeForbidden() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("This should fail!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|employee123")
                                        .claim("name", "Employee User")
                                        .claim("https://vladtech.com/roles", List.of("Employee")))
                                .authorities(new SimpleGrantedAuthority("Employee")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addComment_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("This should fail!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", testPortfolioItem.getPortfolioId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addComment_WithEmptyText_ShouldReturnBadRequest() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "John Client")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ToNonExistentPortfolio_ShouldReturnError() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("Comment on non-existent item");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", "nonexistent-id")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "John Client")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_WithNoNameInToken_ShouldUseFallbackName() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto("Comment without name");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("email", "client@example.com")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName", is("client@example.com")))
                .andExpect(jsonPath("$.text", is("Comment without name")));
    }
}


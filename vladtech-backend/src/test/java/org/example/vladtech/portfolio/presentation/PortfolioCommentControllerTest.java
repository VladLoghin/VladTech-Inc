package org.example.vladtech.portfolio.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.filestorageservice.IFileStorageService;
import org.example.vladtech.portfolio.business.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc
@Import(PortfolioCommentControllerTest.TestSecurityConfig.class)
class PortfolioCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private IFileStorageService fileStorageService;


    private final String portfolioId = "portfolio-1";

    @BeforeEach
    void setup() {
        // IMPORTANT: match the controller's actual call order:
        // addComment(portfolioId, text, userId, authorName)
        when(portfolioService.addComment(
                eq(portfolioId),
                anyString(), // text
                anyString(), // userId
                anyString()  // authorName
        )).thenAnswer(invocation -> {
            String text = invocation.getArgument(1);
            String authorUserId = invocation.getArgument(2);
            String authorName = invocation.getArgument(3);

            PortfolioCommentDto dto = new PortfolioCommentDto();
            dto.setAuthorUserId(authorUserId);
            dto.setAuthorName(authorName);
            dto.setText(text);
            dto.setTimestamp(Instant.now());
            return dto;
        });

        when(portfolioService.addComment(
                eq("nonexistent-id"),
                anyString(), // text
                anyString(), // userId
                anyString()  // authorName
        )).thenThrow(new ResponseStatusException(NOT_FOUND, "Portfolio not found"));
    }

    @Test
    void addComment_AsClient_ShouldAddCommentSuccessfully() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("Great work on this project!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", portfolioId)
                        .with(csrf())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "John Client")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("John Client"))
                .andExpect(jsonPath("$.authorUserId").value("auth0|client123"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.text").value("Great work on this project!"));

        verify(portfolioService, times(1))
                .addComment(eq(portfolioId),
                        eq("Great work on this project!"),
                        eq("auth0|client123"),
                        eq("John Client"));
    }

    @Test
    void addComment_AsAdmin_ShouldAddCommentSuccessfully() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("Admin feedback here!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", portfolioId)
                        .with(csrf())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Admin User"))
                .andExpect(jsonPath("$.authorUserId").value("auth0|admin123"))
                .andExpect(jsonPath("$.text").value("Admin feedback here!"));

        verify(portfolioService, times(1))
                .addComment(eq(portfolioId),
                        eq("Admin feedback here!"),
                        eq("auth0|admin123"),
                        eq("Admin User"));
    }

    @Disabled("Permissions need to be overhauled")
    @Test
    void addComment_AsEmployee_ShouldBeForbidden() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("This should fail!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", portfolioId)
                        .with(csrf())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|employee123")
                                        .claim("name", "Employee User")
                                        .claim("https://vladtech.com/roles", List.of("Employee")))
                                .authorities(new SimpleGrantedAuthority("Employee")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void addComment_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("This should fail!");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", portfolioId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void addComment_WithEmptyText_ShouldReturnBadRequest() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", portfolioId)
                        .with(csrf())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "John Client")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void addComment_ToNonExistentPortfolio_ShouldReturnError() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("Comment on non-existent item");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", "nonexistent-id")
                        .with(csrf())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "John Client")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(portfolioService, times(1))
                .addComment(eq("nonexistent-id"),
                        eq("Comment on non-existent item"),
                        eq("auth0|client123"),
                        eq("John Client"));
    }

    @Test
    void addComment_WithNoNameInToken_ShouldUseFallbackName() throws Exception {
        AddCommentRequestDto request = new AddCommentRequestDto();
        request.setText("Comment without name");

        mockMvc.perform(post("/api/portfolio/{portfolioId}/comments", portfolioId)
                        .with(csrf())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("email", "client@example.com")
                                        .claim("https://vladtech.com/roles", List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("client@example.com"))
                .andExpect(jsonPath("$.authorUserId").value("auth0|client123"))
                .andExpect(jsonPath("$.text").value("Comment without name"));

        verify(portfolioService, times(1))
                .addComment(eq(portfolioId),
                        eq("Comment without name"),
                        eq("auth0|client123"),
                        eq("client@example.com"));
    }

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

        /**
         * Needed so the context can start when oauth2ResourceServer().jwt() is enabled.
         * SecurityMockMvcRequestPostProcessors.jwt() will set the Authentication, so this won't really be used.
         */
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-sub")
                    .build();
        }
    }
}

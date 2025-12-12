package org.example.vladtech.portfolio.presentation;

import org.example.vladtech.portfolio.data.PortfolioItem;
import org.example.vladtech.portfolio.data.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class DeletePortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioRepository portfolioRepository;

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
    void deletePortfolio_AsAdmin_ShouldDeleteSuccessfully() throws Exception {
        // Verify portfolio exists before deletion
        assertTrue(portfolioRepository.findById(testPortfolioItem.getPortfolioId()).isPresent());

        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        // Verify portfolio was deleted
        assertFalse(portfolioRepository.findById(testPortfolioItem.getPortfolioId()).isPresent());
    }

    @Test
    void deletePortfolio_AsClient_ShouldBeForbidden() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "Client User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client"))))
                .andExpect(status().isForbidden());

        // Verify portfolio still exists
        assertTrue(portfolioRepository.findById(testPortfolioItem.getPortfolioId()).isPresent());
    }

    @Test
    void deletePortfolio_AsEmployee_ShouldBeForbidden() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|employee123")
                                        .claim("name", "Employee User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Employee")))
                                .authorities(new SimpleGrantedAuthority("Employee"))))
                .andExpect(status().isForbidden());

        // Verify portfolio still exists
        assertTrue(portfolioRepository.findById(testPortfolioItem.getPortfolioId()).isPresent());
    }

    @Test
    void deletePortfolio_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId()))
                .andExpect(status().isUnauthorized());

        // Verify portfolio still exists
        assertTrue(portfolioRepository.findById(testPortfolioItem.getPortfolioId()).isPresent());
    }

    @Test
    void deletePortfolio_NonExistentPortfolio_ShouldReturnNotFound() throws Exception {
        String nonExistentId = "nonexistent-id-12345";

        mockMvc.perform(delete("/api/portfolio/{portfolioId}", nonExistentId)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePortfolio_MultipleTimes_ShouldReturnNotFoundOnSecondAttempt() throws Exception {
        // First deletion should succeed
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        // Second deletion should fail with not found
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePortfolio_WithComments_ShouldDeleteSuccessfully() throws Exception {
        // Add comments to the portfolio item
        testPortfolioItem.getComments().add(new org.example.vladtech.portfolio.data.PortfolioComment(
                "John Doe",
                "auth0|client123",
                java.time.Instant.now(),
                "Great work!"
        ));
        portfolioRepository.save(testPortfolioItem);

        // Verify portfolio exists with comments
        PortfolioItem retrieved = portfolioRepository.findById(testPortfolioItem.getPortfolioId()).orElseThrow();
        assertEquals(1, retrieved.getComments().size());

        // Delete the portfolio
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        // Verify portfolio was deleted
        assertFalse(portfolioRepository.findById(testPortfolioItem.getPortfolioId()).isPresent());
    }

    @Test
    void deletePortfolio_VerifyRepositoryEmpty_AfterDeletion() throws Exception {
        // Should have 1 item initially
        assertEquals(1, portfolioRepository.count());

        mockMvc.perform(delete("/api/portfolio/{portfolioId}", testPortfolioItem.getPortfolioId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        // Should have 0 items after deletion
        assertEquals(0, portfolioRepository.count());
    }
}


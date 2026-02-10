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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc
class DeletePortfolioControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private IFileStorageService fileStorageService;


    @BeforeEach
    void setup() {
        // admin delete success
        doNothing().when(portfolioService).deletePortfolioItem("existing-id");

        // not found
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Not found"
        )).when(portfolioService).deletePortfolioItem("missing-id");
    }

    @Test
    void deletePortfolio_AsAdmin_ShouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        verify(portfolioService, times(1)).deletePortfolioItem("existing-id");
    }

    @Disabled("Permissions need to be overhauled")
    @Test
    void deletePortfolio_AsClient_ShouldBeForbidden() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioService);
    }

    @Disabled("Permissions need to be overhauled")
    @Test
    void deletePortfolio_AsEmployee_ShouldBeForbidden() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Employee"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void deletePortfolio_WithoutAuthentication_ShouldBeFound() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", "existing-id")
                        .with(csrf()))
                .andExpect(status().isFound());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void deletePortfolio_NonExistentPortfolio_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/portfolio/{portfolioId}", "missing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNotFound());

        verify(portfolioService, times(1)).deletePortfolioItem("missing-id");
    }
}

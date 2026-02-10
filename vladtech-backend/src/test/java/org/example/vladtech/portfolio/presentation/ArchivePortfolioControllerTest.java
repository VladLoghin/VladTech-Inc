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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc
class ArchivePortfolioControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private IFileStorageService fileStorageService;


    @BeforeEach
    void setup() {
        // admin archive success
        doNothing().when(portfolioService).archivePortfolioItem("existing-id");

        // not found
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Not found"
        )).when(portfolioService).archivePortfolioItem("missing-id");

        // unarchive setup
        doNothing().when(portfolioService).unarchivePortfolioItem("existing-id");
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Not found"
        )).when(portfolioService).unarchivePortfolioItem("missing-id");

        // get archived items setup
        PortfolioResponseDto archivedItem = new PortfolioResponseDto();
        archivedItem.setPortfolioId("archived-1");
        archivedItem.setTitle("Archived Item");
        when(portfolioService.getArchivedPortfolioItems()).thenReturn(List.of(archivedItem));
    }

    @Test
    void archivePortfolio_AsAdmin_ShouldArchiveSuccessfully() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/archive", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        verify(portfolioService, times(1)).archivePortfolioItem("existing-id");
    }

    @Disabled("Permissions need to be overhauled")
    @Test
    void archivePortfolio_AsClient_ShouldBeForbidden() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/archive", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Client"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioService);
    }

    @Disabled("Permissions need to be overhauled")
    @Test
    void archivePortfolio_AsEmployee_ShouldBeForbidden() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/archive", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Employee"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void archivePortfolio_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        // OAuth2 security redirects unauthenticated requests to login page (302)
        mockMvc.perform(put("/api/portfolio/{portfolioId}/archive", "existing-id")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void archivePortfolio_NonExistentPortfolio_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/archive", "missing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNotFound());

        verify(portfolioService, times(1)).archivePortfolioItem("missing-id");
    }

    // Unarchive tests
    @Test
    void unarchivePortfolio_AsAdmin_ShouldUnarchiveSuccessfully() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/unarchive", "existing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNoContent());

        verify(portfolioService, times(1)).unarchivePortfolioItem("existing-id");
    }

    @Test
    void unarchivePortfolio_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/unarchive", "existing-id")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void unarchivePortfolio_NonExistentPortfolio_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/portfolio/{portfolioId}/unarchive", "missing-id")
                        .with(csrf())
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isNotFound());

        verify(portfolioService, times(1)).unarchivePortfolioItem("missing-id");
    }

    // Get archived items tests
    @Test
    void getArchivedPortfolioItems_AsAdmin_ShouldReturnArchivedItems() throws Exception {
        mockMvc.perform(get("/api/portfolio/archived")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].portfolioId").value("archived-1"));

        verify(portfolioService, times(1)).getArchivedPortfolioItems();
    }

    @Test
    void getArchivedPortfolioItems_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/portfolio/archived"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(portfolioService);
    }
}

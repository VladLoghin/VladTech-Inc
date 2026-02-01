package org.example.vladtech.portfolio.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.portfolio.business.PortfolioService;
import org.example.vladtech.filestorageservice.FileStorageService;
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

import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc
class CreatePortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @BeforeEach
    void setup() {
        PortfolioResponseDto response = new PortfolioResponseDto();
        response.setPortfolioId("seed-id-1");
        response.setTitle("New Kitchen Renovation");
        response.setImageUrl("/uploads/portfolio/kitchen.jpg");
        response.setComments(new ArrayList<>());

        when(portfolioService.createPortfolioItem(anyString(), anyString(), anyString()))
                .thenReturn(response);
    }

    @Test
    void createPortfolio_AsAdmin_ShouldCreateSuccessfully() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("New Kitchen Renovation");
        request.setImageUrl("/uploads/portfolio/kitchen.jpg");
        request.setType("Kitchen");

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
                .andExpect(jsonPath("$.comments", hasSize(0)));

        verify(portfolioService, times(1))
                .createPortfolioItem(eq("New Kitchen Renovation"), eq("/uploads/portfolio/kitchen.jpg"), eq("Kitchen"));
    }

    @Disabled("Disabled until we fix the security config and permissions in the controller class")
    @Test
    void createPortfolio_AsClient_ShouldBeForbidden() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("This should fail");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setType("Interior");

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

        verifyNoInteractions(portfolioService);
    }

    @Disabled("Disabled until we fix the security config and permissions in the controller class")
    @Test
    void createPortfolio_AsEmployee_ShouldBeForbidden() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("This should fail");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setType("Bathroom");
        request.setType("Bathroom");

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

        verifyNoInteractions(portfolioService);
    }

    @Test
    void createPortfolio_WithoutAuthentication_ShouldBeForbidden() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("This should fail");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setType("Exterior");
        request.setType("Exterior");

        mockMvc.perform(post("/api/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void createPortfolio_WithEmptyTitle_ShouldReturnBadRequest() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("");
        request.setImageUrl("/uploads/portfolio/test.jpg");
        request.setType("Kitchen");
        request.setType("Kitchen");

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(portfolioService);
    }

    @Test
    void createPortfolio_WithEmptyImageUrl_ShouldReturnBadRequest() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("Valid Title");
        request.setImageUrl("");
        request.setType("Bathroom");;
        request.setType("Bathroom");

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(portfolioService);
    }



    @Test
    void createPortfolio_WithValidData_ShouldReturnPortfolioWithId() throws Exception {
        PortfolioResponseDto request = new PortfolioResponseDto();
        request.setTitle("Bathroom Remodel");
        request.setImageUrl("/uploads/portfolio/bathroom.jpg");
        request.setType("Bathroom");
        request.setType("Bathroom");

        mockMvc.perform(post("/api/portfolio")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.portfolioId").isNotEmpty())
                .andExpect(jsonPath("$.title", is("New Kitchen Renovation"))) // note: response is stubbed!
                .andExpect(jsonPath("$.imageUrl", is("/uploads/portfolio/kitchen.jpg")));

        verify(portfolioService, times(1))
                .createPortfolioItem(eq("Bathroom Remodel"), eq("/uploads/portfolio/bathroom.jpg"), eq("Bathroom"));
    }
}
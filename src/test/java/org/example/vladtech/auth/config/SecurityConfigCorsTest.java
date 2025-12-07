package org.example.vladtech.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigCorsTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void corsConfig_AllowsCorrectOrigins() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/me");

        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);

        assertTrue(config.getAllowedOrigins().contains("http://localhost:5173"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedHeaders().contains("*"));
    }
}
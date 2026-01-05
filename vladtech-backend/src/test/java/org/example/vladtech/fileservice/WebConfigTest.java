package org.example.vladtech.fileservice;

import org.example.vladtech.filestorageservice.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @Mock
    private CorsRegistry corsRegistry;

    @Captor
    private ArgumentCaptor<String[]> locationsCaptor;

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
        ReflectionTestUtils.setField(webConfig, "maxUploadSize", 10485760L);
    }

    @Test
    void addResourceHandlers_ShouldConfigureUploadsPaths() {
        // Arrange
        when(resourceHandlerRegistry.addResourceHandler(anyString()))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.addResourceLocations(any(String[].class)))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.setCachePeriod(anyInt()))
                .thenReturn(resourceHandlerRegistration);

        // Act
        webConfig.addResourceHandlers(resourceHandlerRegistry);

        // Assert
        verify(resourceHandlerRegistry).addResourceHandler("/uploads/**");
        verify(resourceHandlerRegistration).addResourceLocations("file:uploads/");
        verify(resourceHandlerRegistration).setCachePeriod(604800);
    }

    @Test
    void addResourceHandlers_ShouldSetCachePeriodTo7Days() {
        // Arrange
        when(resourceHandlerRegistry.addResourceHandler(anyString()))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.addResourceLocations(any(String[].class)))
                .thenReturn(resourceHandlerRegistration);
        when(resourceHandlerRegistration.setCachePeriod(anyInt()))
                .thenReturn(resourceHandlerRegistration);

        // Act
        webConfig.addResourceHandlers(resourceHandlerRegistry);

        // Assert
        ArgumentCaptor<Integer> cachePeriodCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(resourceHandlerRegistration).setCachePeriod(cachePeriodCaptor.capture());
        assertEquals(604800, cachePeriodCaptor.getValue()); // 7 days in seconds
    }

    @Test
    void addCorsMappings_ShouldConfigureCorsForReviewsEndpoint() {
        // Arrange
        TestCorsRegistration corsRegistration = new TestCorsRegistration();
        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);

        // Act
        webConfig.addCorsMappings(corsRegistry);

        // Assert
        verify(corsRegistry).addMapping("/uploads/reviews/**");
        assertTrue(corsRegistration.allowedOrigins.contains("http://localhost:3000"));
        assertTrue(corsRegistration.allowedOrigins.contains("http://localhost:5173"));
        assertTrue(corsRegistration.allowedMethods.contains("GET"));
        assertTrue(corsRegistration.allowedMethods.contains("POST"));
        assertTrue(corsRegistration.allowedMethods.contains("DELETE"));
        assertTrue(corsRegistration.allowedMethods.contains("OPTIONS"));
        assertTrue(corsRegistration.allowedHeaders.contains("*"));
        assertEquals(3600L, corsRegistration.maxAge);
    }

    @Test
    void webConfig_ShouldHaveDefaultMaxUploadSize() {
        // Assert
        Long maxUploadSize = (Long) ReflectionTestUtils.getField(webConfig, "maxUploadSize");
        assertEquals(10485760L, maxUploadSize);
    }

    // Helper class to capture CORS configuration
    private static class TestCorsRegistration extends org.springframework.web.servlet.config.annotation.CorsRegistration {
        List<String> allowedOrigins;
        List<String> allowedMethods;
        List<String> allowedHeaders;
        Long maxAge;

        public TestCorsRegistration() {
            super("/uploads/reviews/**");
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowedOrigins(String... origins) {
            this.allowedOrigins = Arrays.asList(origins);
            return this;
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowedMethods(String... methods) {
            this.allowedMethods = Arrays.asList(methods);
            return this;
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration allowedHeaders(String... headers) {
            this.allowedHeaders = Arrays.asList(headers);
            return this;
        }

        @Override
        public org.springframework.web.servlet.config.annotation.CorsRegistration maxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }
    }
}
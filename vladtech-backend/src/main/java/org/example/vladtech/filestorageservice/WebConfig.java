package org.example.vladtech.filestorageservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${filestorage.max-upload-size:10485760}") // 10MB default
    private long maxUploadSize;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS for file uploads if needed
        registry.addMapping("/uploads/reviews/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "https://vladtech-inc-fudvj.ondigitalocean.app") // Add your frontend URLs
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
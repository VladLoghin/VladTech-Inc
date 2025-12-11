package org.example.vladtech.filestorageservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle /images/** for backward compatibility (reviews)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/reviews/");

        // Handle /uploads/** for both reviews and portfolio
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
    }


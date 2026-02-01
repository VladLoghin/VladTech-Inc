package org.example.vladtech.portfolio.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-time script to update existing portfolio items with null types to have a default type.
 * This will run on application startup and update all portfolio items that have null type field.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdatePortfolioTypes implements CommandLineRunner {

    private final PortfolioRepository portfolioRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for portfolio items with null types...");
        
        List<PortfolioItem> allItems = portfolioRepository.findAll();
        int updatedCount = 0;
        
        for (PortfolioItem item : allItems) {
            if (item.getType() == null || item.getType().trim().isEmpty()) {
                // Set default type based on title keywords, or default to "Interior"
                String type = inferTypeFromTitle(item.getTitle());
                item.setType(type);
                portfolioRepository.save(item);
                updatedCount++;
                log.info("Updated portfolio item '{}' with type: {}", item.getTitle(), type);
            }
        }
        
        if (updatedCount > 0) {
            log.info("✅ Successfully updated {} portfolio items with default types", updatedCount);
        } else {
            log.info("✅ All portfolio items already have types assigned");
        }
    }
    
    private String inferTypeFromTitle(String title) {
        if (title == null) {
            return "Interior";
        }
        
        String titleLower = title.toLowerCase();
        
        // Check for kitchen keywords
        if (titleLower.contains("kitchen") || titleLower.contains("cook")) {
            return "Kitchen";
        }
        
        // Check for bathroom keywords
        if (titleLower.contains("bathroom") || titleLower.contains("bath") || 
            titleLower.contains("shower") || titleLower.contains("toilet")) {
            return "Bathroom";
        }
        
        // Check for exterior keywords
        if (titleLower.contains("yard") || titleLower.contains("outdoor") || 
            titleLower.contains("garden") || titleLower.contains("exterior") ||
            titleLower.contains("roof") || titleLower.contains("siding") ||
            titleLower.contains("deck") || titleLower.contains("patio")) {
            return "Exterior/Yard";
        }
        
        // Default to Interior
        return "Interior";
    }
}

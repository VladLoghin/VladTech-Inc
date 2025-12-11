package org.example.vladtech.portfolio.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.portfolio.business.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<PortfolioResponseDto>> getAllPortfolioItems() {
        log.info("GET request to /api/portfolio - Fetching all portfolio items");
        List<PortfolioResponseDto> portfolioItems = portfolioService.getAllPortfolioItems();
        return ResponseEntity.ok(portfolioItems);
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolioItemById(@PathVariable String portfolioId) {
        log.info("GET request to /api/portfolio/{} - Fetching portfolio item", portfolioId);
        PortfolioResponseDto portfolioItem = portfolioService.getPortfolioItemById(portfolioId);
        return ResponseEntity.ok(portfolioItem);
    }
}


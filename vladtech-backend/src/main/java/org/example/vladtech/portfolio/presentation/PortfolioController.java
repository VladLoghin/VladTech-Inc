package org.example.vladtech.portfolio.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.portfolio.business.PortfolioService;
import org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private static final String UPLOAD_DIR = "uploads/portfolio/";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("POST request to /api/portfolio/upload - Uploading image: {}", file.getOriginalFilename());

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path
            String imageUrl = "/" + UPLOAD_DIR + filename;
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);

            log.info("Image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @PostMapping
    public ResponseEntity<PortfolioResponseDto> createPortfolioItem(
            @Valid @RequestBody PortfolioResponseDto request) {
        log.info("POST request to /api/portfolio - Creating new portfolio item: {}", request.getTitle());

        PortfolioResponseDto createdItem = portfolioService.createPortfolioItem(
                request.getTitle(),
                request.getImageUrl(),
                request.getRating()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolioItem(@PathVariable String portfolioId) {
        log.info("DELETE request to /api/portfolio/{} - Deleting portfolio item", portfolioId);
        portfolioService.deletePortfolioItem(portfolioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{portfolioId}/comments")
    public ResponseEntity<PortfolioCommentDto> addComment(
            @PathVariable String portfolioId,
            @Valid @RequestBody AddCommentRequestDto request,
            Authentication authentication) {

        log.info("POST request to /api/portfolio/{}/comments - Adding comment", portfolioId);

        // Extract user info from JWT token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        
        // Use authorName from request if provided, otherwise extract from JWT
        String userName = request.getAuthorName();
        
        if (userName == null || userName.trim().isEmpty()) {
            // Fallback: Try to get username from JWT claims
            userName = jwt.getClaimAsString("nickname");
            
            if (userName == null || userName.isEmpty()) {
                userName = jwt.getClaimAsString("name");
                if (userName == null || userName.isEmpty()) {
                    userName = jwt.getClaimAsString("email");
                    if (userName == null || userName.isEmpty()) {
                        userName = "Anonymous User";
                    }
                }
            }
        }
        
        log.info("Comment author: {} (userId: {})", userName, userId);

        PortfolioCommentDto comment = portfolioService.addComment(
                portfolioId,
                request.getText(),
                userId,
                userName
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<String> handlePortfolioNotFound(PortfolioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}


package org.example.vladtech.portfolio.presentation;

import com.mongodb.lang.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vladtech.portfolio.business.PortfolioService;
import org.example.vladtech.portfolio.exceptions.PortfolioNotFoundException;
import org.example.vladtech.filestorageservice.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Permissions need to be overhauled*/

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final FileStorageService fileStorageService; // use GridFS for all uploads

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("POST request to /api/portfolio/upload - Uploading image: {}", 
                (file != null ? file.getOriginalFilename() : "null"));

        // Validate that the file is present and not empty before processing
        if (file == null || file.isEmpty()) {
            log.warn("POST request to /api/portfolio/upload with null or empty file");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Uploaded file must not be null or empty");
            return ResponseEntity.badRequest().body(error);
        }
        try {
            // Save file into GridFS via FileStorageService
            String id = fileStorageService.save(file);

            // Return the GridFS-backed URL under /api/uploads/portfolio/{id} so front-end can request it
            String imageUrl = "/api/uploads/portfolio/" + id;
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("id", id);
            response.put("filename", file.getOriginalFilename());

            log.info("Image uploaded successfully to GridFS: {} -> {}", file.getOriginalFilename(), imageUrl);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (IOException e) {
            log.error("Failed to upload image to GridFS", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponseDto>> getAllPortfolioItems(
            @RequestParam(required = false) String type) {
        log.info("GET request to /api/portfolio - Fetching portfolio items" + (type != null ? " with type: " + type : ""));

        List<PortfolioResponseDto> portfolioItems;
        if (type != null && !type.trim().isEmpty()) {
            portfolioItems = portfolioService.getPortfolioItemsByType(type);
        } else {
            portfolioItems = portfolioService.getAllPortfolioItems();
        }

        return ResponseEntity.ok(portfolioItems);
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolioItemById(@PathVariable String portfolioId) {
        log.info("GET request to /api/portfolio/{} - Fetching portfolio item", portfolioId);
        PortfolioResponseDto portfolioItem = portfolioService.getPortfolioItemById(portfolioId);
        return ResponseEntity.ok(portfolioItem);
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<PortfolioResponseDto> createPortfolioItem(
            @Valid @RequestBody PortfolioResponseDto request) {
        log.info("POST request to /api/portfolio - Creating new portfolio item: {} with type: {}", request.getTitle(), request.getType());

        PortfolioResponseDto createdItem = portfolioService.createPortfolioItem(
                request.getTitle(),
                request.getImageUrl(),
                request.getType()
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


package org.example.vladtech.filestorageservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.bson.Document; // explicit import to use Document type

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/reviews")
    public ResponseEntity<?> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = fileStorageService.save(file);

            Map<String, String> response = new HashMap<>();
            response.put("id", fileId);
            response.put("url", "/uploads/reviews/" + fileId);
            response.put("filename", file.getOriginalFilename());

            log.info("File uploaded successfully: id={}, filename={}", fileId, file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<StreamingResponseBody> getReviewImage(
            @PathVariable String id,
            @RequestParam(value = "download", defaultValue = "false") boolean forceDownload) {

        try {
            FileStorageService.FileResourceWithMetadata fileData =
                    fileStorageService.loadResourceWithMetadata(id);

            Resource resource = fileData.getResource();
            Document metadata = fileData.getMetadata();
            String contentType = fileData.getContentType();

            // Get original filename from metadata or fallback to resource filename
            String originalFilename = (metadata != null && metadata.containsKey("originalFilename"))
                    ? metadata.getString("originalFilename")
                    : (resource != null ? resource.getFilename() : "file");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // Try to set content length if available — but keep it optional.
            try {
                long contentLength = resource.contentLength();
                headers.setContentLength(contentLength);
            } catch (IOException e) {
                // Don't propagate; content length may not be determinable for GridFS streams.
                log.warn("Could not determine content length for file: {}", id, e);
            }

            // Set content disposition (inline for browser display, attachment for download)
            String disposition = forceDownload ? "attachment" : "inline";
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    disposition + "; filename=\"" + originalFilename + "\"");

            // Set cache headers for better performance (images typically don't change)
            headers.setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());

            log.debug("Serving file: id={}, filename={}, contentType={}", id, originalFilename, contentType);

            // Use StreamingResponseBody to stream the resource content directly. This avoids
            // the default message converters trying to re-query content length (which may
            // throw IOException for GridFsResource) and causing a 500.
            StreamingResponseBody body = outputStream -> {
                try (var in = resource.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            };

            return new ResponseEntity<>(body, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid file ID requested: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (FileNotFoundException e) {
            log.warn("File not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error retrieving file: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReviewImage(@PathVariable String id) {
        try {
            fileStorageService.delete(id);
            log.info("File deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Invalid file ID for deletion: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (FileNotFoundException e) {
            log.warn("File not found for deletion: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deleting file: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reviews/{id}/metadata")
    public ResponseEntity<?> getFileMetadata(@PathVariable String id) {
        try {
            Document metadata = fileStorageService.getMetadata(id);

            Map<String, Object> response = new HashMap<>();
            if (metadata != null) {
                response.put("originalFilename", metadata.getString("originalFilename"));
                response.put("contentType", metadata.getString("contentType"));
                response.put("size", metadata.getLong("size"));
                response.put("uploadedAt", metadata.getLong("uploadedAt"));
            }
            response.put("id", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid file ID for metadata: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (FileNotFoundException e) {
            log.warn("File not found for metadata: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error retrieving metadata: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Portfolio endpoints (same class, separate URL space) ---

    @GetMapping("/portfolio/{id}")
    public ResponseEntity<StreamingResponseBody> getPortfolioImage(
            @PathVariable String id,
            @RequestParam(value = "download", defaultValue = "false") boolean forceDownload) {

        try {
            FileStorageService.FileResourceWithMetadata fileData =
                    fileStorageService.loadResourceWithMetadata(id);

            Resource resource = fileData.getResource();
            Document metadata = fileData.getMetadata();
            String contentType = fileData.getContentType();

            String originalFilename = (metadata != null && metadata.containsKey("originalFilename"))
                    ? metadata.getString("originalFilename")
                    : (resource != null ? resource.getFilename() : "file");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            try {
                long contentLength = resource.contentLength();
                headers.setContentLength(contentLength);
            } catch (IOException e) {
                log.warn("Could not determine content length for portfolio file: {}", id, e);
            }

            String disposition = forceDownload ? "attachment" : "inline";
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    disposition + "; filename=\"" + originalFilename + "\"");

            headers.setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());

            StreamingResponseBody body = outputStream -> {
                try (var in = resource.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            };

            return new ResponseEntity<>(body, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid portfolio file ID requested: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (FileNotFoundException e) {
            log.warn("Portfolio file not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error retrieving portfolio file: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/portfolio/{id}/metadata")
    public ResponseEntity<?> getPortfolioFileMetadata(@PathVariable String id) {
        try {
            Document metadata = fileStorageService.getMetadata(id);
            Map<String, Object> response = new HashMap<>();
            if (metadata != null) {
                response.put("originalFilename", metadata.getString("originalFilename"));
                response.put("contentType", metadata.getString("contentType"));
                response.put("size", metadata.getLong("size"));
                response.put("uploadedAt", metadata.getLong("uploadedAt"));
            }
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid portfolio file ID for metadata: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (FileNotFoundException e) {
            log.warn("Portfolio file not found for metadata: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving portfolio metadata: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/portfolio/{id}")
    public ResponseEntity<?> deletePortfolioFile(@PathVariable String id) {
        try {
            fileStorageService.delete(id);
            log.info("Portfolio file deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid portfolio file ID for deletion: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (FileNotFoundException e) {
            log.warn("Portfolio file not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting portfolio file: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
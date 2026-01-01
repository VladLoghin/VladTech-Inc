package org.example.vladtech.filestorageservice;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    // File size limit: 10MB
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed image types for review uploads
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final int MAX_FILENAME_LENGTH = 255;

    @Value("${filestorage.bucket:reviews}")
    private String bucket;

    public String save(MultipartFile file) throws IOException {
        // Validate file size
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " +
                    (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WebP)");
        }

        // Validate and sanitize filename
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        if (!isValidFilename(originalName)) {
            throw new IllegalArgumentException("Invalid filename. Use only letters, numbers, dots, hyphens, and underscores");
        }

        if (originalName.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("Filename too long");
        }

        // Sanitize: replace spaces and ensure no path traversal
        String cleanName = sanitizeFilename(originalName);

        Document metadata = new Document();
        metadata.put("originalFilename", cleanName);
        metadata.put("contentType", contentType);
        metadata.put("size", file.getSize());
        metadata.put("uploadedAt", System.currentTimeMillis());

        try {
            ObjectId id = gridFsTemplate.store(file.getInputStream(), cleanName, contentType, metadata);
            log.info("File saved successfully: id={}, filename={}, size={}",
                    id.toHexString(), cleanName, file.getSize());
            return id.toHexString();
        } catch (IOException e) {
            log.error("Failed to save file: {}", originalName, e);
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }
    }

    public FileResourceWithMetadata loadResourceWithMetadata(String id) throws FileNotFoundException {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid id format");
        }

        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
        if (gridFsFile == null) {
            throw new FileNotFoundException("File not found: " + id);
        }

        GridFsResource resource = gridFsOperations.getResource(gridFsFile);
        Document metadata = gridFsFile.getMetadata();
        String contentType = gridFsFile.getMetadata() != null && gridFsFile.getMetadata().containsKey("contentType")
                ? gridFsFile.getMetadata().getString("contentType")
                : (resource.getContentType() != null ? resource.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return new FileResourceWithMetadata(resource, metadata, contentType);
    }

    public GridFsResource loadAsResource(String id) throws FileNotFoundException {
        return loadResourceWithMetadata(id).getResource();
    }

    public Document getMetadata(String id) throws FileNotFoundException {
        return loadResourceWithMetadata(id).getMetadata();
    }

    public void delete(String id) throws FileNotFoundException {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid id format");
        }

        // Verify file exists before deleting
        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
        if (gridFsFile == null) {
            throw new FileNotFoundException("File not found: " + id);
        }

        gridFsTemplate.delete(new Query(Criteria.where("_id").is(objectId)));
        log.info("File deleted successfully: id={}", id);
    }

    private boolean isValidFilename(String filename) {
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        // Check for null bytes and control characters
        if (filename.contains("\0") || filename.chars().anyMatch(ch -> ch < 32 && ch != 9)) {
            return false;
        }

        // Allow alphanumeric, dots, hyphens, underscores, and spaces
        // We'll sanitize spaces later, but they're acceptable in the original name
        return filename.matches("^[a-zA-Z0-9._\\-\\s]+$");
    }

    private String sanitizeFilename(String filename) {
        // Replace spaces with underscores and remove any problematic characters
        return filename.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._\\-]", "");
    }

    // Inner class to hold resource and metadata together
    public static class FileResourceWithMetadata {
        private final GridFsResource resource;
        private final Document metadata;
        private final String contentType;

        public FileResourceWithMetadata(GridFsResource resource, Document metadata, String contentType) {
            this.resource = resource;
            this.metadata = metadata;
            this.contentType = contentType;
        }

        public GridFsResource getResource() {
            return resource;
        }

        public Document getMetadata() {
            return metadata;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
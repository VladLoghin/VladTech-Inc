package org.example.vladtech.filestorageservice;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Profile("!prod")
@RequiredArgsConstructor
public class GridFsFileStorageService implements IFileStorageService {

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

    @Override
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
        log.info("save: original filename='{}', size={}, contentType={}", originalName, file.getSize(), file.getContentType());
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        // Reject path traversal and path separators in the original filename immediately.
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename. Use only letters, numbers, dots, hyphens, and underscores");
        }

        // Sanitize first: replace spaces and strip problematic characters
        String cleanName = sanitizeFilename(originalName);

        if (cleanName == null || cleanName.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        // Validate sanitized filename for control characters and allowed characters
        if (!isValidFilename(cleanName) || !cleanName.matches("^[a-zA-Z0-9._\\-]+$")) {
            throw new IllegalArgumentException("Invalid filename. Use only letters, numbers, dots, hyphens, and underscores");
        }

        if (cleanName.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("Filename too long");
        }

        Document metadata = new Document();
        metadata.put("originalFilename", cleanName);
        metadata.put("contentType", contentType);
        metadata.put("size", file.getSize());
        metadata.put("uploadedAt", System.currentTimeMillis());

        try {
            ObjectId id = gridFsTemplate.store(file.getInputStream(), cleanName, contentType, metadata);
            if (id == null) {
                log.error("GridFsTemplate returned null id when storing file: {}", cleanName);
                throw new IOException("Failed to save file: null id from storage");
            }
            log.info("File saved successfully: id={}, filename={}, size={}",
                    id.toHexString(), cleanName, file.getSize());
            return id.toHexString();
        } catch (Exception e) {
            // Wrap any exception as IOException to keep the API contract for callers/tests
            log.error("Failed to save file: {}", originalName, e);
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }
    }

    @Override
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

        GridFsResource gridFsResource = null;
        Document metadata = null;

        final int MAX_ATTEMPTS = 3;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS && gridFsResource == null; attempt++) {
            try {
                gridFsResource = gridFsOperations.getResource(gridFsFile);
            } catch (Exception e) {
                log.debug("Attempt {}: could not get GridFsResource for file {}: {}", attempt, id, e.getMessage());
            }

            try {
                metadata = gridFsFile.getMetadata();
            } catch (Exception e) {
                log.debug("Attempt {}: unable to read metadata for file {}: {}", attempt, id, e.getMessage());
            }

            if (gridFsResource == null) {
                try {
                    GridFSFile refetched = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
                    if (refetched != null) {
                        gridFsFile = refetched;
                        try { metadata = gridFsFile.getMetadata(); } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    log.debug("Attempt {}: refetch failed for file {}: {}", attempt, id, e.getMessage());
                }
            }
        }

        if (metadata == null) {
            try { metadata = gridFsFile.getMetadata(); } catch (Exception ignored) {}
        }

        String contentType;
        if (metadata != null && metadata.containsKey("contentType")) {
            contentType = metadata.getString("contentType");
        } else if (gridFsResource != null) {
            try {
                String rt = gridFsResource.getContentType();
                contentType = (rt != null) ? rt : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            } catch (Exception e) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        } else {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        Resource resource = (gridFsResource != null) ? gridFsResource : new ByteArrayResource(new byte[0]);
        
        // Convert Document to Map for interface compatibility
        Map<String, Object> metadataMap = new HashMap<>();
        if (metadata != null) {
            metadataMap.putAll(metadata);
        }
        
        FileResourceWithMetadata result = new FileResourceWithMetadata(resource, metadataMap, contentType);
        log.info("loadResourceWithMetadata returning: {} (resource={}, metadata={}, contentType={})", result, resource, metadataMap, contentType);
        return result;
    }

    @Override
    public Map<String, Object> getMetadata(String id) throws FileNotFoundException {
        return loadResourceWithMetadata(id).getMetadata();
    }

    @Override
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
        // Check for null bytes and control characters
        if (filename.contains("\0") || filename.chars().anyMatch(ch -> ch < 32 && ch != 9)) {
            return false;
        }
        return true;
    }

    private String sanitizeFilename(String filename) {
        // Replace spaces with underscores and remove any problematic characters
        return filename.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._\\-]", "");
    }
}

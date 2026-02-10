package org.example.vladtech.filestorageservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Profile({"prod", "s3-test"})
@RequiredArgsConstructor
public class S3FileStorageService implements IFileStorageService {

    private final S3Client s3Client;

    @Value("${vladtech.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${vladtech.aws.s3.folder-prefix:images}")
    private String folderPrefix;

    // File size limit: 10MB
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed image types
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final int MAX_FILENAME_LENGTH = 255;

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

        // Reject path traversal and path separators
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename. Use only letters, numbers, dots, hyphens, and underscores");
        }

        // Sanitize filename
        String cleanName = sanitizeFilename(originalName);

        if (cleanName == null || cleanName.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        if (!isValidFilename(cleanName) || !cleanName.matches("^[a-zA-Z0-9._\\-]+$")) {
            throw new IllegalArgumentException("Invalid filename. Use only letters, numbers, dots, hyphens, and underscores");
        }

        if (cleanName.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("Filename too long");
        }

        // Generate unique key for S3
        String fileId = UUID.randomUUID().toString();
        String s3Key = folderPrefix + "/" + fileId;

        // Prepare metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("originalfilename", cleanName);
        metadata.put("uploadedAt", String.valueOf(System.currentTimeMillis()));

        try {
            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File saved successfully to S3: id={}, filename={}, size={}", fileId, cleanName, file.getSize());
            return fileId;

        } catch (Exception e) {
            log.error("Failed to save file to S3: {}", originalName, e);
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }
    }

    @Override
    public FileResourceWithMetadata loadResourceWithMetadata(String id) throws FileNotFoundException {
        log.info("Loading file with id: {}", id);
        String s3Key = folderPrefix + "/" + id;

        try {
            // Get object metadata first
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

            // Get the actual object
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            byte[] objectBytes = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

            // Build metadata map
            Map<String, Object> metadata = new HashMap<>();
            if (headObjectResponse.metadata() != null) {
                metadata.putAll(headObjectResponse.metadata());
            }
            metadata.put("size", headObjectResponse.contentLength());
            metadata.put("contentType", headObjectResponse.contentType());

            String contentType = headObjectResponse.contentType() != null 
                    ? headObjectResponse.contentType() 
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            Resource resource = new ByteArrayResource(objectBytes);

            log.info("File loaded successfully from S3: id={}", id);
            return new FileResourceWithMetadata(resource, metadata, contentType);

        } catch (NoSuchKeyException e) {
            log.warn("File not found in S3: {}", id);
            throw new FileNotFoundException("File not found: " + id);
        } catch (Exception e) {
            log.error("Error loading file from S3: {}", id, e);
            throw new FileNotFoundException("Error loading file: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMetadata(String id) throws FileNotFoundException {
        log.info("Getting metadata for file with id: {}", id);
        return loadResourceWithMetadata(id).getMetadata();
    }

    @Override
    public void delete(String id) throws FileNotFoundException {
        String s3Key = folderPrefix + "/" + id;

        try {
            // Check if object exists first
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);

            // Delete the object
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("File deleted successfully from S3: id={}", id);

        } catch (NoSuchKeyException e) {
            log.warn("File not found in S3 for deletion: {}", id);
            throw new FileNotFoundException("File not found: " + id);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", id, e);
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
    }

    private boolean isValidFilename(String filename) {
        if (filename.contains("\0") || filename.chars().anyMatch(ch -> ch < 32 && ch != 9)) {
            return false;
        }
        return true;
    }

    private String sanitizeFilename(String filename) {
        return filename.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._\\-]", "");
    }
}

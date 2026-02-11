package org.example.vladtech.filestorageservice;

import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * DTO to hold resource and metadata together.
 * Supports both GridFS and S3 storage backends.
 */
public class FileResourceWithMetadata {
    private final Resource resource;
    private final Map<String, Object> metadata;
    private final String contentType;

    public FileResourceWithMetadata(Resource resource, Map<String, Object> metadata, String contentType) {
        this.resource = resource;
        this.metadata = metadata;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getContentType() {
        return contentType;
    }
}

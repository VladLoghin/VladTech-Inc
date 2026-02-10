package org.example.vladtech.filestorageservice;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * File storage service interface.
 * Implementations can use different storage backends (GridFS, S3, etc.)
 */
public interface IFileStorageService {
    
    /**
     * Save a file to storage.
     * @param file the multipart file to save
     * @return unique identifier for the saved file
     * @throws IOException if saving fails
     */
    String save(MultipartFile file) throws IOException;

    /**
     * Load a file resource with its metadata.
     * @param id the file identifier
     * @return resource with metadata
     * @throws FileNotFoundException if file not found
     */
    FileResourceWithMetadata loadResourceWithMetadata(String id) throws FileNotFoundException;

    /**
     * Get metadata for a file.
     * @param id the file identifier
     * @return metadata map
     * @throws FileNotFoundException if file not found
     */
    Map<String, Object> getMetadata(String id) throws FileNotFoundException;

    /**
     * Delete a file from storage.
     * @param id the file identifier
     * @throws FileNotFoundException if file not found
     */
    void delete(String id) throws FileNotFoundException;
}

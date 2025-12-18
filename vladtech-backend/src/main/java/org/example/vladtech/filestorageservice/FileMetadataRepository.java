package org.example.vladtech.filestorageservice;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
}
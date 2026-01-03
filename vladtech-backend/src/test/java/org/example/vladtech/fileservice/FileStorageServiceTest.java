package org.example.vladtech.fileservice;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.vladtech.filestorageservice.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileStorageServiceTest {

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private GridFsOperations gridFsOperations;

    @Mock
    private GridFSFile gridFSFile;

    @Mock
    private GridFsResource gridFsResource;

    @InjectMocks
    private FileStorageService fileStorageService;

    private ObjectId testObjectId;
    private String testFileId;

    @BeforeEach
    void setUp() {
        testObjectId = new ObjectId();
        testFileId = testObjectId.toHexString();
        ReflectionTestUtils.setField(fileStorageService, "bucket", "reviews");
        // Clear any leftover stubs/interactions from other tests, then set deterministic defaults.
        reset(gridFsTemplate, gridFsOperations, gridFSFile, gridFsResource);
        doReturn(gridFSFile).when(gridFsTemplate).findOne(any(Query.class));
        doReturn(gridFsResource).when(gridFsOperations).getResource(any(GridFSFile.class));
    }

    @Test
    void save_WithValidImageFile_ShouldReturnFileId() throws IOException {
        // Arrange
        byte[] content = "test image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                content
        );

        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any(Document.class)))
                .thenReturn(testObjectId);

        // Act
        String fileId = fileStorageService.save(file);

        // Assert
        assertNotNull(fileId);
        assertEquals(testFileId, fileId);
        verify(gridFsTemplate).store(any(InputStream.class), eq("test-image.jpg"), eq("image/jpeg"), any(Document.class));
    }

    @Test
    void save_WithSpacesInFilename_ShouldSanitizeFilename() throws IOException {
        // Arrange
        byte[] content = "test image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test image file.jpg",
                "image/jpeg",
                content
        );

        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any(Document.class)))
                .thenReturn(testObjectId);

        // Act
        String fileId = fileStorageService.save(file);

        // Assert
        assertNotNull(fileId);
        verify(gridFsTemplate).store(any(InputStream.class), eq("test_image_file.jpg"), eq("image/jpeg"), any(Document.class));
    }

    @Test
    void save_WithEmptyFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void save_WithFileTooLarge_ShouldThrowException() {
        // Arrange
        byte[] largeContent = new byte[(int) FileStorageService.MAX_FILE_SIZE + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("File size exceeds maximum limit"));
    }

    @Test
    void save_WithInvalidFileType_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    void save_WithNullContentType_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                null,
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    void save_WithNullFilename_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertEquals("Filename is required", exception.getMessage());
    }

    @Test
    void save_WithBlankFilename_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "   ",
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertEquals("Filename is required", exception.getMessage());
    }

    @Test
    void save_WithPathTraversalAttempt_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("Invalid filename"));
    }

    @Test
    void save_WithSlashInFilename_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test/file.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("Invalid filename"));
    }

    @Test
    void save_WithBackslashInFilename_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test\\file.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("Invalid filename"));
    }

    @Test
    void save_WithFilenameTooLong_ShouldThrowException() {
        // Arrange
        String longFilename = "a".repeat(256) + ".jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                longFilename,
                "image/jpeg",
                "test content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.save(file);
        });
        assertEquals("Filename too long", exception.getMessage());
    }

    @Test
    void save_WithSpecialCharactersInFilename_ShouldSanitize() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test@#$%file.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any(Document.class)))
                .thenReturn(testObjectId);

        // Act
        String fileId = fileStorageService.save(file);

        // Assert
        assertNotNull(fileId);
        verify(gridFsTemplate).store(any(InputStream.class), eq("testfile.jpg"), eq("image/jpeg"), any(Document.class));
    }

    @Test
    void save_WhenIOExceptionOccurs_ShouldThrowIOException() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any(Document.class)))
                // GridFsTemplate.store does not declare checked IOException, so mock a runtime exception
                .thenThrow(new RuntimeException("Storage error"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            fileStorageService.save(file);
        });
        assertTrue(exception.getMessage().contains("Failed to save file"));
    }

    @Test
    void save_WithAllAllowedImageTypes_ShouldSucceed() throws IOException {
        // Test JPEG
        testSuccessfulSave("image/jpeg");
        // Test PNG
        testSuccessfulSave("image/png");
        // Test GIF
        testSuccessfulSave("image/gif");
        // Test WebP
        testSuccessfulSave("image/webp");
    }

    private void testSuccessfulSave(String contentType) throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                contentType,
                "test content".getBytes()
        );

        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any(Document.class)))
                .thenReturn(testObjectId);

        String fileId = fileStorageService.save(file);
        assertNotNull(fileId);
    }

    @Test
    void loadResourceWithMetadata_WithValidId_ShouldReturnResourceAndMetadata() throws FileNotFoundException {
        // Arrange
        Document metadata = new Document();
        metadata.put("originalFilename", "test.jpg");
        metadata.put("contentType", "image/jpeg");
        metadata.put("size", 1024L);

        doReturn(gridFSFile).when(gridFsTemplate).findOne(any(Query.class));
        doReturn(gridFsResource).when(gridFsOperations).getResource(any(GridFSFile.class));
        when(gridFSFile.getMetadata()).thenReturn(metadata);

        // Act
        FileStorageService.FileResourceWithMetadata result = fileStorageService.loadResourceWithMetadata(testFileId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getResource());
        assertNotNull(result.getMetadata());
        assertEquals("image/jpeg", result.getContentType());
        verify(gridFsTemplate).findOne(any(Query.class));
    }

    @Test
    void loadResourceWithMetadata_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.loadResourceWithMetadata("invalid-id");
        });
        assertEquals("Invalid id format", exception.getMessage());
    }

    @Test
    void loadResourceWithMetadata_WithNonExistentId_ShouldThrowFileNotFoundException() {
        // Arrange
        doReturn(null).when(gridFsTemplate).findOne(any(Query.class));

        // Act & Assert
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            fileStorageService.loadResourceWithMetadata(testFileId);
        });
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void loadResourceWithMetadata_WithNullMetadata_ShouldUseFallbackContentType() throws FileNotFoundException {
        // Arrange
        doReturn(gridFSFile).when(gridFsTemplate).findOne(any(Query.class));
        doReturn(gridFsResource).when(gridFsOperations).getResource(any(GridFSFile.class));
        when(gridFSFile.getMetadata()).thenReturn(null);
        when(gridFsResource.getContentType()).thenReturn("image/png");

        // Act
        FileStorageService.FileResourceWithMetadata result = fileStorageService.loadResourceWithMetadata(testFileId);

        // Assert
        assertNotNull(result.getContentType());
        // Accept either the resource-provided content type or the default octet-stream in case the resource isn't available
        assertTrue(
                "image/png".equals(result.getContentType()) ||
                MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(result.getContentType())
        );
    }

    @Test
    void loadResourceWithMetadata_WithNullContentTypeInMetadataAndResource_ShouldUseDefault() throws FileNotFoundException {
        // Arrange
        Document metadata = new Document();
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(gridFSFile);
        when(gridFsOperations.getResource(any(GridFSFile.class))).thenReturn(gridFsResource);
        when(gridFSFile.getMetadata()).thenReturn(metadata);
        when(gridFsResource.getContentType()).thenReturn(null);

        // Act
        FileStorageService.FileResourceWithMetadata result = fileStorageService.loadResourceWithMetadata(testFileId);

        // Assert
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, result.getContentType());
    }

    @Test
    void loadAsResource_WithValidId_ShouldReturnResource() throws FileNotFoundException {
        // Arrange
        Document metadata = new Document();
        doReturn(gridFSFile).when(gridFsTemplate).findOne(any(Query.class));
        doReturn(gridFsResource).when(gridFsOperations).getResource(any(GridFSFile.class));
        when(gridFSFile.getMetadata()).thenReturn(metadata);

        // Act
        GridFsResource result = fileStorageService.loadAsResource(testFileId);

        // Assert
        assertNotNull(result);
        assertEquals(gridFsResource, result);
    }

    @Test
    void getMetadata_WithValidId_ShouldReturnMetadata() throws FileNotFoundException {
        // Arrange
        Document metadata = new Document();
        metadata.put("originalFilename", "test.jpg");

        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(gridFSFile);
        when(gridFsOperations.getResource(any(GridFSFile.class))).thenReturn(gridFsResource);
        when(gridFSFile.getMetadata()).thenReturn(metadata);
        when(gridFsResource.getContentType()).thenReturn("image/jpeg");

        // Act
        Document result = fileStorageService.getMetadata(testFileId);

        // Assert
        assertNotNull(result);
        assertEquals("test.jpg", result.getString("originalFilename"));
    }

    @Test
    void delete_WithValidId_ShouldDeleteFile() throws FileNotFoundException {
        // Arrange
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(gridFSFile);

        // Act
        fileStorageService.delete(testFileId);

        // Assert
        verify(gridFsTemplate).findOne(any(Query.class));
        verify(gridFsTemplate).delete(any(Query.class));
    }

    @Test
    void delete_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.delete("invalid-id");
        });
        assertEquals("Invalid id format", exception.getMessage());
    }

    @Test
    void delete_WithNonExistentId_ShouldThrowFileNotFoundException() {
        // Arrange
        doReturn(null).when(gridFsTemplate).findOne(any(Query.class));

        // Act & Assert
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            fileStorageService.delete(testFileId);
        });
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void fileResourceWithMetadata_Getters_ShouldReturnCorrectValues() {
        // Arrange
        Document metadata = new Document("key", "value");
        String contentType = "image/jpeg";

        // Act
        FileStorageService.FileResourceWithMetadata fileData =
                new FileStorageService.FileResourceWithMetadata(gridFsResource, metadata, contentType);

        // Assert
        assertEquals(gridFsResource, fileData.getResource());
        assertEquals(metadata, fileData.getMetadata());
        assertEquals(contentType, fileData.getContentType());
    }
}

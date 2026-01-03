package org.example.vladtech.fileservice;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.vladtech.filestorageservice.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private GridFsResource gridFsResource;

    private String testFileId;
    private Document testMetadata;

    @BeforeEach
    void setUp() {
        testFileId = new ObjectId().toHexString();
        testMetadata = new Document();
        testMetadata.put("originalFilename", "test.jpg");
        testMetadata.put("contentType", "image/jpeg");
        testMetadata.put("size", 1024L);
        testMetadata.put("uploadedAt", System.currentTimeMillis());
    }

    @Test
    void uploadReviewImage_WithValidFile_ShouldReturnCreated() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.save(any())).thenReturn(testFileId);

        // Act & Assert
        mockMvc.perform(multipart("/uploads/reviews")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testFileId))
                .andExpect(jsonPath("$.url").value("/uploads/reviews/" + testFileId))
                .andExpect(jsonPath("$.filename").value("test.jpg"));

        verify(fileStorageService).save(any());
    }

    @Test
    void uploadReviewImage_WithInvalidFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        when(fileStorageService.save(any())).thenThrow(new IllegalArgumentException("Invalid file type"));

        // Act & Assert
        mockMvc.perform(multipart("/uploads/reviews")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid file type"));
    }

    @Test
    void uploadReviewImage_WhenIOExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.save(any())).thenThrow(new IOException("Storage error"));

        // Act & Assert
        mockMvc.perform(multipart("/uploads/reviews")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to upload file"));
    }

    @Test
    void getReviewImage_WithValidId_ShouldReturnImage() throws Exception {
        // Arrange
        byte[] content = "test image content".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        FileStorageService.FileResourceWithMetadata fileData =
                new FileStorageService.FileResourceWithMetadata(gridFsResource, testMetadata, "image/jpeg");

        when(fileStorageService.loadResourceWithMetadata(testFileId)).thenReturn(fileData);
        when(gridFsResource.getInputStream()).thenReturn(inputStream);
        when(gridFsResource.contentLength()).thenReturn((long) content.length);
        when(gridFsResource.getFilename()).thenReturn("test.jpg");

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Length", String.valueOf(content.length)))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.jpg\""))
                .andExpect(header().exists("Cache-Control"));

        verify(fileStorageService).loadResourceWithMetadata(testFileId);
    }

    @Test
    void getReviewImage_WithDownloadParameter_ShouldReturnAsAttachment() throws Exception {
        // Arrange
        byte[] content = "test image content".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        FileStorageService.FileResourceWithMetadata fileData =
                new FileStorageService.FileResourceWithMetadata(gridFsResource, testMetadata, "image/jpeg");

        when(fileStorageService.loadResourceWithMetadata(testFileId)).thenReturn(fileData);
        when(gridFsResource.getInputStream()).thenReturn(inputStream);
        when(gridFsResource.contentLength()).thenReturn((long) content.length);

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId)
                        .param("download", "true")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.jpg\""));
    }

    @Test
    void getReviewImage_WithoutMetadata_ShouldUseFallbackFilename() throws Exception {
        // Arrange
        byte[] content = "test image content".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        FileStorageService.FileResourceWithMetadata fileData =
                new FileStorageService.FileResourceWithMetadata(gridFsResource, null, "image/jpeg");

        when(fileStorageService.loadResourceWithMetadata(testFileId)).thenReturn(fileData);
        when(gridFsResource.getInputStream()).thenReturn(inputStream);
        when(gridFsResource.contentLength()).thenReturn((long) content.length);
        when(gridFsResource.getFilename()).thenReturn("fallback.jpg");

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"fallback.jpg\""));
    }

    @Test
    void getReviewImage_WhenContentLengthThrowsIOException_ShouldStillReturnImage() throws Exception {
        // Arrange
        byte[] content = "test image content".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        FileStorageService.FileResourceWithMetadata fileData =
                new FileStorageService.FileResourceWithMetadata(gridFsResource, testMetadata, "image/jpeg");

        when(fileStorageService.loadResourceWithMetadata(testFileId)).thenReturn(fileData);
        when(gridFsResource.getInputStream()).thenReturn(inputStream);
        when(gridFsResource.contentLength()).thenThrow(new IOException("Cannot determine length"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Content-Length"));
    }

    @Test
    void getReviewImage_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(fileStorageService.loadResourceWithMetadata(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid id format"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/invalid-id")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewImage_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(fileStorageService.loadResourceWithMetadata(anyString()))
                .thenThrow(new FileNotFoundException("File not found"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReviewImage_WhenUnexpectedExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(fileStorageService.loadResourceWithMetadata(anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteReviewImage_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(fileStorageService).delete(testFileId);

        // Act & Assert
        mockMvc.perform(delete("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isNoContent());

        verify(fileStorageService).delete(testFileId);
    }

    @Test
    void deleteReviewImage_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid id format"))
                .when(fileStorageService).delete(anyString());

        // Act & Assert
        mockMvc.perform(delete("/uploads/reviews/invalid-id")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReviewImage_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new FileNotFoundException("File not found"))
                .when(fileStorageService).delete(anyString());

        // Act & Assert
        mockMvc.perform(delete("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReviewImage_WhenUnexpectedExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
                .when(fileStorageService).delete(anyString());

        // Act & Assert
        mockMvc.perform(delete("/uploads/reviews/" + testFileId)
                        .with(jwt()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getFileMetadata_WithValidId_ShouldReturnMetadata() throws Exception {
        // Arrange
        when(fileStorageService.getMetadata(testFileId)).thenReturn(testMetadata);

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId + "/metadata")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFileId))
                .andExpect(jsonPath("$.originalFilename").value("test.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.size").value(1024))
                .andExpect(jsonPath("$.uploadedAt").exists());

        verify(fileStorageService).getMetadata(testFileId);
    }

    @Test
    void getFileMetadata_WithNullMetadata_ShouldReturnOnlyId() throws Exception {
        // Arrange
        when(fileStorageService.getMetadata(testFileId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId + "/metadata")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFileId))
                .andExpect(jsonPath("$.originalFilename").doesNotExist());
    }

    @Test
    void getFileMetadata_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(fileStorageService.getMetadata(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid id format"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/invalid-id/metadata")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFileMetadata_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(fileStorageService.getMetadata(anyString()))
                .thenThrow(new FileNotFoundException("File not found"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId + "/metadata")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFileMetadata_WhenUnexpectedExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(fileStorageService.getMetadata(anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/uploads/reviews/" + testFileId + "/metadata")
                        .with(jwt()))
                .andExpect(status().isInternalServerError());
    }
}
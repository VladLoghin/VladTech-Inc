package org.example.vladtech.filestorageservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3FileStorageService s3FileStorageService;

    private final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(s3FileStorageService, "bucketName", BUCKET_NAME);
    }

    @Test
    void save_WhenValidFile_ShouldUploadToS3() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        String fileId = s3FileStorageService.save(file);

        assertNotNull(fileId);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void loadResourceWithMetadata_WhenFileExists_ShouldReturnResource() throws IOException {
        String fileId = "test-id";
        byte[] content = "test content".getBytes();
        
        HeadObjectResponse headResponse = HeadObjectResponse.builder()
                .contentLength((long) content.length)
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .metadata(Map.of("originalfilename", "test.jpg"))
                .build();

        GetObjectResponse getResponse = GetObjectResponse.builder().build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(getResponse, content);

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headResponse);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        FileResourceWithMetadata result = s3FileStorageService.loadResourceWithMetadata(fileId);

        assertNotNull(result);
        assertEquals(MediaType.IMAGE_JPEG_VALUE, result.getContentType());
        assertEquals("test.jpg", result.getMetadata().get("originalfilename"));
        assertEquals(content.length, result.getResource().contentLength());
    }

    @Test
    void loadResourceWithMetadata_WhenKeyNotFound_ShouldThrowFileNotFoundException() {
        String fileId = "missing-id";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        assertThrows(FileNotFoundException.class, () -> s3FileStorageService.loadResourceWithMetadata(fileId));
    }

    @Test
    void delete_WhenFileExists_ShouldDeleteObject() throws FileNotFoundException {
        String fileId = "delete-id";
        
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());

        s3FileStorageService.delete(fileId);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void delete_WhenFileDoesNotExist_ShouldThrowFileNotFoundException() {
        String fileId = "missing-delete-id";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        assertThrows(FileNotFoundException.class, () -> s3FileStorageService.delete(fileId));
    }
}

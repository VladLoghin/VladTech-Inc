package org.example.vladtech.fileservice;

import org.example.vladtech.filestorageservice.IFileStorageService;
import org.example.vladtech.filestorageservice.FileResourceWithMetadata;
import org.example.vladtech.filestorageservice.ProjectUploadsController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectUploadsControllerTest {

    @Mock
    private IFileStorageService fileStorageService;

    @Test
    void getProjectPhoto_returns200_withInlineDisposition_andCacheHeaders_andContentTypeFromMetadata() throws Exception {
        // arrange
        ProjectUploadsController controller = new ProjectUploadsController(fileStorageService);

        String projectIdentifier = "PROJ-1";
        String photoId = "abc123";

        Resource resource = new ByteArrayResource("img".getBytes());
        String contentType = "image/jpeg";
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", contentType);
        FileResourceWithMetadata fm = new FileResourceWithMetadata(resource, metadata, contentType);

        when(fileStorageService.loadResourceWithMetadata(photoId)).thenReturn(fm);

        // act
        ResponseEntity<Resource> response = controller.getProjectPhoto(projectIdentifier, photoId);

        // assert
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resource, response.getBody());

        MediaType actualType = response.getHeaders().getContentType();
        assertNotNull(actualType);
        assertEquals(MediaType.IMAGE_JPEG, actualType);

        String cd = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertEquals("inline; filename=\"" + photoId + "\"", cd);

        String cacheControl = response.getHeaders().getCacheControl();
        assertNotNull(cacheControl);
        assertTrue(cacheControl.contains("public"));
        assertTrue(cacheControl.toLowerCase().contains("max-age"));

        verify(fileStorageService).loadResourceWithMetadata(photoId);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    void getProjectPhoto_whenContentTypeInvalid_fallsBackToOctetStream() throws Exception {
        // arrange
        ProjectUploadsController controller = new ProjectUploadsController(fileStorageService);

        String projectIdentifier = "PROJ-1";
        String photoId = "abc123";

        Resource resource = new ByteArrayResource("img".getBytes());
        String invalidContentType = "not/a real type;;;";

        Map<String, Object> metadata = new HashMap<>();
        FileResourceWithMetadata fm = new FileResourceWithMetadata(resource, metadata, invalidContentType);

        when(fileStorageService.loadResourceWithMetadata(photoId)).thenReturn(fm);

        // act
        ResponseEntity<Resource> response = controller.getProjectPhoto(projectIdentifier, photoId);

        // assert
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resource, response.getBody());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());

        verify(fileStorageService).loadResourceWithMetadata(photoId);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    void getProjectPhoto_propagatesFileNotFoundException() throws Exception {
        // arrange
        ProjectUploadsController controller = new ProjectUploadsController(fileStorageService);

        String projectIdentifier = "PROJ-1";
        String photoId = "missing";

        when(fileStorageService.loadResourceWithMetadata(photoId))
                .thenThrow(new FileNotFoundException("File not found: " + photoId));

        // act + assert
        assertThrows(FileNotFoundException.class, () ->
                controller.getProjectPhoto(projectIdentifier, photoId));

        verify(fileStorageService).loadResourceWithMetadata(photoId);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    void getProjectPhoto_stillBuildsResponse_evenIfProjectIdentifierIsDifferent() throws Exception {
        // NOTE: controller does not use projectIdentifier at all, but we still test it doesn't break anything.
        ProjectUploadsController controller = new ProjectUploadsController(fileStorageService);

        String projectIdentifier = "PROJ-999";
        String photoId = "abc123";

        Resource resource = new ByteArrayResource("img".getBytes());
        String contentType = "image/png";
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", contentType);
        FileResourceWithMetadata fm = new FileResourceWithMetadata(resource, metadata, contentType);

        when(fileStorageService.loadResourceWithMetadata(photoId)).thenReturn(fm);

        ResponseEntity<Resource> response = controller.getProjectPhoto(projectIdentifier, photoId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertSame(resource, response.getBody());

        verify(fileStorageService).loadResourceWithMetadata(photoId);
        verifyNoMoreInteractions(fileStorageService);
    }
}

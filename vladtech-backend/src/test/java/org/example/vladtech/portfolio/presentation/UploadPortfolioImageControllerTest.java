package org.example.vladtech.portfolio.presentation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class UploadPortfolioImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String UPLOAD_DIR = "uploads/portfolio/";

    @BeforeEach
    void setup() throws Exception {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        // Clean up uploaded test files
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (Files.exists(uploadPath)) {
            try (Stream<Path> paths = Files.walk(uploadPath)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .filter(File::isFile) // Only delete files, not the directory
                        .forEach(File::delete);
            }
        }
    }

    @Test
    void uploadImage_AsAdmin_ShouldUploadSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.imageUrl", startsWith("/uploads/portfolio/")))
                .andExpect(jsonPath("$.imageUrl", endsWith(".jpg")));
    }

    @Test
    void uploadImage_AsClient_ShouldBeForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|client123")
                                        .claim("name", "Client User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Client")))
                                .authorities(new SimpleGrantedAuthority("Client"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadImage_AsEmployee_ShouldBeForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|employee123")
                                        .claim("name", "Employee User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Employee")))
                                .authorities(new SimpleGrantedAuthority("Employee"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadImage_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadImage_WithPngFile_ShouldUploadSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.imageUrl", endsWith(".png")));
    }

    @Test
    void uploadImage_FileSavedToDisk_ShouldExist() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract imageUrl from response and verify file exists
        String imageUrl = response.substring(response.indexOf("/uploads/"), response.indexOf(".jpg") + 4);
        Path filePath = Paths.get(imageUrl.substring(1)); // Remove leading slash
        assertTrue(Files.exists(filePath), "Uploaded file should exist on disk");
    }

    @Test
    void uploadImage_MultipleFiles_ShouldHaveUniqueNames() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 2".getBytes()
        );

        String response1 = mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file1)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String response2 = mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file2)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotEquals(response1, response2, "Each uploaded file should have a unique name");
    }

    @Test
    void uploadImage_FileWithoutExtension_ShouldHandleGracefully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.imageUrl", startsWith("/uploads/portfolio/")));
    }

    @Test
    void uploadImage_VerifyDirectoryCreation_WhenNotExists() throws Exception {
        // Delete the upload directory first
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (Files.exists(uploadPath)) {
            try (Stream<Path> paths = Files.walk(uploadPath)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portfolio/upload")
                        .file(file)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("auth0|admin123")
                                        .claim("name", "Admin User")
                                        .claim("https://vladtech.com/roles", java.util.List.of("Admin")))
                                .authorities(new SimpleGrantedAuthority("Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists());

        // Verify directory was created
        assertTrue(Files.exists(uploadPath), "Upload directory should be created if it doesn't exist");
    }
}


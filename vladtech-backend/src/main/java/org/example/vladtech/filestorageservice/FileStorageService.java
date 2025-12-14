package org.example.vladtech.filestorageservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Profile("!test")
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir);

        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
    }

    public String save(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.contains("..") ||
                originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String cleanName = originalName.replace(" ", "_");
        String filename = System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID() + "_" +
                cleanName;

        Path dest = root.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }
}


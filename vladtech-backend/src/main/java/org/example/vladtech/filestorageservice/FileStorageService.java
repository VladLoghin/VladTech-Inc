package org.example.vladtech.filestorageservice;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String save(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String cleanName = originalName.replace(" ", "_");
        String uniqueFilename = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + cleanName;

        // Upload file to S3
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        s3Client.putObject(bucketName, uniqueFilename, file.getInputStream(), metadata);

        // Save metadata to MongoDB
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFilename(uniqueFilename);
        fileMetadata.setOriginalName(originalName);
        fileMetadata.setUrl(s3Client.getUrl(bucketName, uniqueFilename).toString());
        fileMetadataRepository.save(fileMetadata);

        return fileMetadata.getUrl();
    }
}


//package org.example.vladtech.filestorageservice;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.Objects;
//
//@Service
//public class FileStorageService {
//
//    private final Path root = Paths.get("uploads/reviews");
//
//    public FileStorageService() throws IOException {
//        if (!Files.exists(root)) {
//            Files.createDirectories(root);
//        }
//    }
//
//    public String save(MultipartFile file) throws IOException {
//        String originalName = file.getOriginalFilename();
//        if (originalName == null || originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
//            throw new IllegalArgumentException("Invalid filename");
//        }
//        String cleanName = originalName.replace(" ", "_");
//        String filename = System.currentTimeMillis() + "_" + java.util.UUID.randomUUID() + "_" + cleanName;
//
//        Path dest = root.resolve(filename);
//        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
//
//        return filename;
//    }
//}

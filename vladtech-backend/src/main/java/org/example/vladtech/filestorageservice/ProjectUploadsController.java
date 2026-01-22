package org.example.vladtech.filestorageservice;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/uploads/projects")
@RequiredArgsConstructor
public class ProjectUploadsController {

    private final FileStorageService fileStorageService;

    // GET /api/uploads/projects/{projectIdentifier}/{photoId}
    @GetMapping("/{projectIdentifier}/{photoId}")
    public ResponseEntity<Resource> getProjectPhoto(
            @PathVariable String projectIdentifier,
            @PathVariable String photoId
    ) throws FileNotFoundException {

        var fm = fileStorageService.loadResourceWithMetadata(photoId);

        MediaType type;
        try {
            type = MediaType.parseMediaType(fm.getContentType());
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photoId + "\"")
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(fm.getResource());
    }
}

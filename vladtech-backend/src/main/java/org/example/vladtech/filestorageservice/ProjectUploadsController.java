package org.example.vladtech.filestorageservice;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/uploads/projects")
@RequiredArgsConstructor
public class ProjectUploadsController {

    private final FileStorageService fileStorageService;
    private final ProjectRepository projectRepository;

    @PreAuthorize("hasAuthority('Admin') or hasAuthority('Employee')")
    @GetMapping("/{projectIdentifier}/{photoId}")
    public ResponseEntity<Resource> getProjectPhoto(
            @PathVariable String projectIdentifier,
            @PathVariable String photoId,
            @AuthenticationPrincipal Jwt jwt
    ) throws FileNotFoundException {

        Project project = projectRepository.findByProjectIdentifier(projectIdentifier)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Ensure photo belongs to the project
        boolean belongs = project.getPhotos() != null &&
                project.getPhotos().stream().anyMatch(p -> photoId.equals(p.getPhotoId()));
        if (!belongs) {
            return ResponseEntity.status(404).build();
        }

        // If employee: must be assigned (Admin can view)
        boolean isAdmin = jwt.getClaimAsStringList("permissions") != null; // ignore if you don't use this
        // Better: use roles claim you already use, example:
        var roles = jwt.getClaimAsStringList("https://vladtech.com/roles");
        boolean adminRole = roles != null && roles.contains("Admin");

        if (!adminRole) {
            String employeeId = jwt.getSubject();
            if (project.getAssignedEmployeeIds() == null || !project.getAssignedEmployeeIds().contains(employeeId)) {
                return ResponseEntity.status(403).build();
            }
        }

        var fm = fileStorageService.loadResourceWithMetadata(photoId);

        String contentType = (fm.getContentType() != null) ? fm.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fm.getResource());
    }
}

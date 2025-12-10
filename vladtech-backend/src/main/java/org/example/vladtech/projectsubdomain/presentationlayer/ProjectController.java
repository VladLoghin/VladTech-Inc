package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectCalendarEntryResponseModel;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping
    public ResponseEntity<List<ProjectResponseModel>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/{projectIdentifier}")
    public ResponseEntity<ProjectResponseModel> getProjectByIdentifier(@PathVariable String projectIdentifier) {
        return ResponseEntity.ok(projectService.getProjectByIdentifier(projectIdentifier));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<ProjectResponseModel> createProject(@RequestBody ProjectRequestModel projectRequestModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(projectRequestModel));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{projectIdentifier}")
    public ResponseEntity<ProjectResponseModel> updateProject(
            @PathVariable String projectIdentifier,
            @RequestBody ProjectRequestModel projectRequestModel) {
        return ResponseEntity.ok(projectService.updateProject(projectIdentifier, projectRequestModel));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{projectIdentifier}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectIdentifier) {
        projectService.deleteProject(projectIdentifier);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/{projectIdentifier}/assign/{employeeId}")
    public ResponseEntity<ProjectResponseModel> assignEmployee(
            @PathVariable String projectIdentifier,
            @PathVariable String employeeId) {
        return ResponseEntity.ok(projectService.assignEmployee(projectIdentifier, employeeId));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/{projectIdentifier}/photos")
    public ResponseEntity<List<PhotoResponseModel>> getProjectPhotos(@PathVariable String projectIdentifier) {
        return ResponseEntity.ok(projectService.getProjectPhotos(projectIdentifier));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/{projectIdentifier}/photos")
    public ResponseEntity<ProjectResponseModel> addProjectPhoto(
            @PathVariable String projectIdentifier,
            @RequestBody PhotoResponseModel photoResponseModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addProjectPhoto(projectIdentifier, photoResponseModel));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{projectIdentifier}/photos/{photoId}")
    public ResponseEntity<Void> deleteProjectPhoto(
            @PathVariable String projectIdentifier,
            @PathVariable String photoId) {
        projectService.deleteProjectPhoto(projectIdentifier, photoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public long getProjectCount() {
        return projectService.getProjectCount();
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/calendar")
    public ResponseEntity<List<ProjectCalendarEntryResponseModel>> getProjectsForCalendar() {
        return ResponseEntity.ok(projectService.getProjectsForCalendar());
    }

}
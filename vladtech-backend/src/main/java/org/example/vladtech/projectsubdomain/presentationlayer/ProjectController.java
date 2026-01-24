package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectCalendarEntryResponseModel;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectState;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponseModel>> searchProjects(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectState state,
            @RequestParam(required = false) ProjectPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(projectService.searchProjects(
                q, status, state, priority,
                org.springframework.data.domain.PageRequest.of(page, size)));
    }

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addProjectPhoto(projectIdentifier, photoResponseModel));
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

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{projectIdentifier}/complete")
    public ResponseEntity<ProjectResponseModel> completeProject(@PathVariable String projectIdentifier) {
        return ResponseEntity.ok(projectService.completeProject(projectIdentifier));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{projectIdentifier}/reactivate")
    public ResponseEntity<ProjectResponseModel> reactivateProject(@PathVariable String projectIdentifier) {
        return ResponseEntity.ok(projectService.reactivateProject(projectIdentifier));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/active")
    public ResponseEntity<List<ProjectResponseModel>> getActiveProjects() {
        return ResponseEntity.ok(projectService.getActiveProjects());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/archived")
    public ResponseEntity<List<ProjectResponseModel>> getArchivedProjects() {
        return ResponseEntity.ok(projectService.getArchivedProjects());
    }

}
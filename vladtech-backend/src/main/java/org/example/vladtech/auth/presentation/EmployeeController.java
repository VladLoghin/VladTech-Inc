package org.example.vladtech.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.auth.service.UserManagementService;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.example.vladtech.projectsubdomain.dataaccesslayer.ProjectStatus;
import org.example.vladtech.projectsubdomain.presentationlayer.ProjectResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserManagementService userManagementService;
    private final ProjectService projectService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/list")
    public ResponseEntity<List<EmployeeSummaryResponseModel>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int perPage
    ) {
        return ResponseEntity.ok(userManagementService.getAllEmployees(page, perPage));
    }

    @PreAuthorize("hasAuthority('Employee')")
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseModel>> getMyProjects(@AuthenticationPrincipal Jwt jwt) {
        String employeeId = jwt.getSubject();
        return ResponseEntity.ok(projectService.getProjectsForEmployee(employeeId));
    }

    @PreAuthorize("hasAuthority('Employee')")
    @PutMapping("/projects/{projectIdentifier}/status")
    public ResponseEntity<ProjectResponseModel> updateMyProjectStatus(
            @PathVariable String projectIdentifier,
            @RequestBody ProjectStatusUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String employeeId = jwt.getSubject();
        ProjectStatus newStatus = ProjectStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(
                projectService.updateProjectStatusForEmployee(projectIdentifier, employeeId, newStatus)
        );
    }
}

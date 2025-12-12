package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.projectsubdomain.businesslayer.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeProjectsController {

    private final ProjectService projectService;

    @PreAuthorize("hasAuthority('Employee')")
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseModel>> getMyProjects(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String employeeId = jwt.getSubject();

        return ResponseEntity.ok(
                projectService.getProjectsForEmployee(employeeId)
        );
    }
}

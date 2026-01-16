package org.example.vladtech.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.auth.service.RoleAssignmentServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/role-assignment")
@RequiredArgsConstructor
public class RoleAssignmentController {

    private final RoleAssignmentServiceImpl roleAssignmentService;

    //do not delete
    @PatchMapping("/users/{userId}/roles/client")
    public String assignClientRole(@PathVariable String userId) {
        roleAssignmentService.assignClientRole(userId);
        return "Client role assigned successfully.";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/users/{userId}/roles/employee")
    public String assignEmployeeRole(@PathVariable String userId) {
        roleAssignmentService.assignEmployeeRole(userId);
        return "Employee role assigned successfully.";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/users/{userId}/roles/admin")
    public String assignAdminRole(@PathVariable String userId) {
        roleAssignmentService.assignAdminRole(userId);
        return "Admin role assigned successfully.";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/users/{userId}/roles/assign/{roleId}")
    public String assignRole(@PathVariable String userId, @PathVariable String roleId) {
        roleAssignmentService.assignRole(userId, roleId);
        return "Role assigned successfully.";
    }

    // Role removal endpoints
    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/users/{userId}/roles/client")
    public String removeClientRole(@PathVariable String userId) {
        roleAssignmentService.removeClientRole(userId);
        return "Client role removed successfully.";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/users/{userId}/roles/employee")
    public String removeEmployeeRole(@PathVariable String userId) {
        roleAssignmentService.removeEmployeeRole(userId);
        return "Employee role removed successfully.";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/users/{userId}/roles/admin")
    public String removeAdminRole(@PathVariable String userId) {
        roleAssignmentService.removeAdminRole(userId);
        return "Admin role removed successfully.";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/users/{userId}/roles/remove/{roleId}")
    public String removeRole(@PathVariable String userId, @PathVariable String roleId) {
        roleAssignmentService.removeRole(userId, roleId);
        return "Role removed successfully.";
    }
}

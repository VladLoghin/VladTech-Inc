package org.example.vladtech.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.auth.service.UserManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Value("${auth0.roles.client}")
    private String clientRoleId;

    @Value("${auth0.roles.employee}")
    private String employeeRoleId;

    @Value("${auth0.roles.admin}")
    private String adminRoleId;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/clients")
    public ResponseEntity<Map<String, Object>> getClientUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int perPage) {
        return ResponseEntity.ok(userManagementService.getUsersByRole(clientRoleId, page, perPage));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getEmployeeUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int perPage) {
        return ResponseEntity.ok(userManagementService.getUsersByRole(employeeRoleId, page, perPage));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/admins")
    public ResponseEntity<Map<String, Object>> getAdminUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int perPage) {
        return ResponseEntity.ok(userManagementService.getUsersByRole(adminRoleId, page, perPage));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/role/{roleId}")
    public ResponseEntity<Map<String, Object>> getUsersByRole(
            @PathVariable String roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int perPage) {
        return ResponseEntity.ok(userManagementService.getUsersByRole(roleId, page, perPage));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String query,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int perPage) {
        return ResponseEntity.ok(userManagementService.searchUsers(query, role, page, perPage));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/without-role/{roleId}")
    public ResponseEntity<Map<String, Object>> getUsersWithoutRole(
            @PathVariable String roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int perPage) {
        return ResponseEntity.ok(userManagementService.getUsersWithoutRole(roleId, page, perPage));
    }
}
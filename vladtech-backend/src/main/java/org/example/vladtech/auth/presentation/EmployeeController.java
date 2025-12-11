package org.example.vladtech.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.auth.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserManagementService userManagementService;

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/list")
    public ResponseEntity<List<EmployeeSummaryResponseModel>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int perPage
    ) {
        return ResponseEntity.ok(userManagementService.getAllEmployees(page, perPage));
    }
}

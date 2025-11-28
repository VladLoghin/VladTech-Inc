package org.example.vladtech.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.example.vladtech.auth.service.RoleAssignmentServiceImpl;
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

}

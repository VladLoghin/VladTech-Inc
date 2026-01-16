package org.example.vladtech.auth.presentation;

import org.example.vladtech.auth.service.RoleAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentControllerTest {

    @Mock
    private RoleAssignmentServiceImpl roleAssignmentService; // must match constructor type

    private RoleAssignmentController controller;

    @BeforeEach
    void setUp() {
        controller = new RoleAssignmentController(roleAssignmentService);
    }

    @Test
    void assignClientRole_returnsSuccessMessage_whenRoleIsAssigned() {
        doNothing().when(roleAssignmentService).assignClientRole("user123");

        var response = controller.assignClientRole("user123");

        assertEquals("Client role assigned successfully.", response);
        verify(roleAssignmentService).assignClientRole("user123");
    }

    @Test
    void assignClientRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .assignClientRole("user123");

        assertThrows(
                IllegalStateException.class,
                () -> controller.assignClientRole("user123"));
    }

    @Test
    void assignEmployeeRole_returnsSuccessMessage_whenRoleIsAssigned() {
        doNothing().when(roleAssignmentService).assignEmployeeRole("user456");

        var response = controller.assignEmployeeRole("user456");

        assertEquals("Employee role assigned successfully.", response);
        verify(roleAssignmentService).assignEmployeeRole("user456");
    }

    @Test
    void assignEmployeeRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .assignEmployeeRole("user456");

        assertThrows(
                IllegalStateException.class,
                () -> controller.assignEmployeeRole("user456"));
    }

    @Test
    void assignAdminRole_returnsSuccessMessage_whenRoleIsAssigned() {
        doNothing().when(roleAssignmentService).assignAdminRole("user789");

        var response = controller.assignAdminRole("user789");

        assertEquals("Admin role assigned successfully.", response);
        verify(roleAssignmentService).assignAdminRole("user789");
    }

    @Test
    void assignAdminRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .assignAdminRole("user789");

        assertThrows(
                IllegalStateException.class,
                () -> controller.assignAdminRole("user789"));
    }

    @Test
    void assignRole_returnsSuccessMessage_whenRoleIsAssigned() {
        String userId = "userABC";
        String roleId = "custom-role-id";
        doNothing().when(roleAssignmentService).assignRole(userId, roleId);

        var response = controller.assignRole(userId, roleId);

        assertEquals("Role assigned successfully.", response);
        verify(roleAssignmentService).assignRole(userId, roleId);
    }

    @Test
    void assignRole_throwsException_whenServiceFails() {
        String userId = "userABC";
        String roleId = "custom-role-id";
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .assignRole(userId, roleId);

        assertThrows(
                IllegalStateException.class,
                () -> controller.assignRole(userId, roleId));
    }

    // Role removal tests

    @Test
    void removeClientRole_returnsSuccessMessage_whenRoleIsRemoved() {
        doNothing().when(roleAssignmentService).removeClientRole("user123");

        var response = controller.removeClientRole("user123");

        assertEquals("Client role removed successfully.", response);
        verify(roleAssignmentService).removeClientRole("user123");
    }

    @Test
    void removeClientRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .removeClientRole("user123");

        assertThrows(
                IllegalStateException.class,
                () -> controller.removeClientRole("user123"));
    }

    @Test
    void removeEmployeeRole_returnsSuccessMessage_whenRoleIsRemoved() {
        doNothing().when(roleAssignmentService).removeEmployeeRole("user456");

        var response = controller.removeEmployeeRole("user456");

        assertEquals("Employee role removed successfully.", response);
        verify(roleAssignmentService).removeEmployeeRole("user456");
    }

    @Test
    void removeEmployeeRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .removeEmployeeRole("user456");

        assertThrows(
                IllegalStateException.class,
                () -> controller.removeEmployeeRole("user456"));
    }

    @Test
    void removeAdminRole_returnsSuccessMessage_whenRoleIsRemoved() {
        doNothing().when(roleAssignmentService).removeAdminRole("user789");

        var response = controller.removeAdminRole("user789");

        assertEquals("Admin role removed successfully.", response);
        verify(roleAssignmentService).removeAdminRole("user789");
    }

    @Test
    void removeAdminRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .removeAdminRole("user789");

        assertThrows(
                IllegalStateException.class,
                () -> controller.removeAdminRole("user789"));
    }

    @Test
    void removeRole_returnsSuccessMessage_whenRoleIsRemoved() {
        String userId = "userXYZ";
        String roleId = "custom-role-id";
        doNothing().when(roleAssignmentService).removeRole(userId, roleId);

        var response = controller.removeRole(userId, roleId);

        assertEquals("Role removed successfully.", response);
        verify(roleAssignmentService).removeRole(userId, roleId);
    }

    @Test
    void removeRole_throwsException_whenServiceFails() {
        String userId = "userXYZ";
        String roleId = "custom-role-id";
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .removeRole(userId, roleId);

        assertThrows(
                IllegalStateException.class,
                () -> controller.removeRole(userId, roleId));
    }
}

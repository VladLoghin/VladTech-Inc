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
    }

    @Test
    void assignClientRole_throwsException_whenServiceFails() {
        doThrow(new IllegalStateException("Failed"))
                .when(roleAssignmentService)
                .assignClientRole("user123");

        assertThrows(
                IllegalStateException.class,
                () -> controller.assignClientRole("user123")
        );
    }
}

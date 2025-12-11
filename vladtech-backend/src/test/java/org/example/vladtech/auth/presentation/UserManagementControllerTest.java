package org.example.vladtech.auth.presentation;

import org.example.vladtech.auth.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    @Mock
    private UserManagementService userManagementService;

    private UserManagementController controller;

    @BeforeEach
    void setUp() {
        controller = new UserManagementController(userManagementService);

        ReflectionTestUtils.setField(controller, "clientRoleId", "client-role-id");
        ReflectionTestUtils.setField(controller, "employeeRoleId", "employee-role-id");
        ReflectionTestUtils.setField(controller, "adminRoleId", "admin-role-id");
    }

    @Test
    void getClientUsers_returnsUsersList_whenServiceReturnsData() {
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(Map.of("user_id", "user1", "email", "user1@example.com")),
                "total", 1,
                "page", 0,
                "perPage", 25
        );

        when(userManagementService.getUsersByRole("client-role-id", 0, 25))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.getClientUsers(0, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userManagementService).getUsersByRole("client-role-id", 0, 25);
    }

    @Test
    void getEmployeeUsers_returnsUsersList_withCustomPagination() {
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(),
                "total", 0,
                "page", 2,
                "perPage", 50
        );

        when(userManagementService.getUsersByRole("employee-role-id", 2, 50))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.getEmployeeUsers(2, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("page"));
        assertEquals(50, response.getBody().get("perPage"));
        verify(userManagementService).getUsersByRole("employee-role-id", 2, 50);
    }

    @Test
    void getAdminUsers_returnsUsersList_whenCalled() {
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(Map.of("user_id", "admin1")),
                "total", 1
        );

        when(userManagementService.getUsersByRole("admin-role-id", 0, 25))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.getAdminUsers(0, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userManagementService).getUsersByRole("admin-role-id", 0, 25);
    }

    @Test
    void getUsersByRole_returnsUsersList_forArbitraryRoleId() {
        String customRoleId = "custom-role-123";
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(Map.of("user_id", "user123")),
                "total", 1
        );

        when(userManagementService.getUsersByRole(customRoleId, 1, 10))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.getUsersByRole(customRoleId, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userManagementService).getUsersByRole(customRoleId, 1, 10);
    }

    @Test
    void searchUsers_returnsSearchResults_whenQueryProvided() {
        String query = "john@example.com";
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(Map.of("email", "john@example.com")),
                "total", 1
        );

        when(userManagementService.searchUsers(query, null, 0, 25))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.searchUsers(query, null, 0, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userManagementService).searchUsers(query, null, 0, 25);
    }

    @Test
    void searchUsers_returnsFilteredResults_whenRoleProvided() {
        String query = "john";
        String role = "clients";
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(Map.of("email", "john@example.com")),
                "total", 1
        );

        when(userManagementService.searchUsers(query, role, 0, 25))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.searchUsers(query, role, 0, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(userManagementService).searchUsers(query, role, 0, 25);
    }

    @Test
    void getUsersWithoutRole_returnsFilteredUsers_whenCalled() {
        String roleId = "client-role-id";
        Map<String, Object> mockResponse = Map.of(
                "users", List.of(
                        Map.of("user_id", "user1", "email", "user1@example.com"),
                        Map.of("user_id", "user2", "email", "user2@example.com")
                ),
                "total", 2
        );

        when(userManagementService.getUsersWithoutRole(roleId, 0, 25))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = controller.getUsersWithoutRole(roleId, 0, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, ((List<?>) response.getBody().get("users")).size());
        verify(userManagementService).getUsersWithoutRole(roleId, 0, 25);
    }

    @Test
    void searchUsers_throwsException_whenServiceFails() {
        when(userManagementService.searchUsers(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalStateException("Failed to search users"));

        assertThrows(IllegalStateException.class,
                () -> controller.searchUsers("test", null, 0, 25));
    }
}
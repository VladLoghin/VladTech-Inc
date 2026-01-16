package org.example.vladtech.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

//do not delete
@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl {

    private final Auth0ManagementTokenService managementTokenService;

    private final RestTemplate restTemplate;

    @Value("${AUTH0_DOMAIN}")
    private String domain;

    @Value("${AUTH0_ROLE_CLIENT}")
    private String clientRoleId;

    @Value("${AUTH0_ROLE_EMPLOYEE}")
    private String employeeRoleId;

    @Value("${AUTH0_ROLE_ADMIN}")
    private String adminRoleId;

    public void assignClientRole(String auth0UserId) {
        assignRole(auth0UserId, clientRoleId);
        System.out.println("Client role assigned to " + auth0UserId);
    }

    public void assignEmployeeRole(String auth0UserId) {
        assignRole(auth0UserId, employeeRoleId);
        System.out.println("Employee role assigned to " + auth0UserId);
    }

    public void assignAdminRole(String auth0UserId) {
        assignRole(auth0UserId, adminRoleId);
        System.out.println("Admin role assigned to " + auth0UserId);
    }

    public void assignRole(String auth0UserId, String roleId) {
        String mgmtToken = managementTokenService.getManagementApiToken();

        String url = "https://" + domain + "/api/v2/roles/" + roleId + "/users";

        Map<String, Object> body = Map.of(
                "users", List.of(auth0UserId));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to assign role: " + response.getStatusCode());
        }
    }

    public void removeClientRole(String auth0UserId) {
        removeRole(auth0UserId, clientRoleId);
        System.out.println("Client role removed from " + auth0UserId);
    }

    public void removeEmployeeRole(String auth0UserId) {
        removeRole(auth0UserId, employeeRoleId);
        System.out.println("Employee role removed from " + auth0UserId);
    }

    public void removeAdminRole(String auth0UserId) {
        removeRole(auth0UserId, adminRoleId);
        System.out.println("Admin role removed from " + auth0UserId);
    }

    public void removeRole(String auth0UserId, String roleId) {
        String mgmtToken = managementTokenService.getManagementApiToken();

        // Auth0 API: DELETE /api/v2/users/{user_id}/roles with { "roles": ["role_id"] }
        String url = "https://" + domain + "/api/v2/users/" + auth0UserId + "/roles";

        Map<String, Object> body = Map.of(
                "roles", List.of(roleId));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to remove role: " + response.getStatusCode());
        }
    }
}

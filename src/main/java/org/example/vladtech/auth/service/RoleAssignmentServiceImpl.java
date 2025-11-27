package org.example.vladtech.auth.service;

import lombok.RequiredArgsConstructor;
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

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.default.client.roleId}")
    private String clientRoleId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Assign the "Client" role (clientRoleId) to the given Auth0 user id, e.g. "auth0|69211..."
     */
    public void assignClientRole(String auth0UserId) {

        String mgmtToken = managementTokenService.getManagementApiToken();

        String url = "https://" + domain + "/api/v2/roles/" + clientRoleId + "/users";

        Map<String, Object> body = Map.of(
                "users", List.of(auth0UserId)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response =
                restTemplate.postForEntity(url, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to assign Client role: " + response.getStatusCode());
        }

        System.out.println("✅ Client role assigned to " + auth0UserId);
    }
}

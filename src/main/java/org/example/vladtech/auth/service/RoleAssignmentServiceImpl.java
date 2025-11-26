package org.example.vladtech.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final Auth0ManagementTokenService tokenService;

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.default.client.roleId}")
    private String clientRoleId;

    private final RestTemplate rest = new RestTemplate();

    public void assignClientRole(String auth0UserId) {

        String token = tokenService.getManagementApiToken();

        String url = "https://" + domain + "/api/v2/roles/" + clientRoleId + "/users";

        Map<String, Object> body = Map.of(
                "users", List.of(auth0UserId)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        rest.postForEntity(url, entity, Void.class);

        System.out.println("Client role assigned to user: " + auth0UserId);
    }
}


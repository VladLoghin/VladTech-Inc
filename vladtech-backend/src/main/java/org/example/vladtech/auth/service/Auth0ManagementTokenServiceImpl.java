package org.example.vladtech.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

//do not delete

@Service
@RequiredArgsConstructor
public class Auth0ManagementTokenServiceImpl implements Auth0ManagementTokenService {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.mgmt.clientId}")
    private String clientId;

    @Value("${auth0.mgmt.clientSecret}")
    private String clientSecret;

    @Value("${auth0.mgmt.audience}")
    private String audience;

    private final RestTemplate restTemplate;

    //private final Auth0ManagementTokenService managementTokenService;

    @Override
    public String getManagementApiToken() {

        String url = "https://" + domain + "/oauth/token";

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "audience", audience,
                "grant_type", "client_credentials"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to get management token: " + response.getStatusCode());
        }

        Object token = response.getBody().get("access_token");
        if (token == null) {
            throw new IllegalStateException("No access_token in management token response");
        }

        return token.toString();
    }

}

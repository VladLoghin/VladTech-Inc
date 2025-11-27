package org.example.vladtech.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class Auth0ManagementTokenServiceImpl implements Auth0ManagementTokenService {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.mgmt.clientId}")
    private String clientId;

    @Value("${auth0.mgmt.clientSecret}")
    private String clientSecret;

    @Value("$auth0.mgmt.audience")
    private String audience;

    private final RestTemplate rest = new RestTemplate();

    public String getManagementApiToken() {
        String url = "https://" + domain + "/oauth/token";

        Map<String, String> request = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "audience", audience,
                "grant_type", "client_credentials"
        );

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "audience", audience,
                "grant_type", "client_credentials"
        );

        Map<String, Object> response = rest.postForObject(url, body, Map.class);
        return (String) response.get("access_token");
    }

}

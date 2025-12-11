package org.example.vladtech.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.example.vladtech.auth.presentation.EmployeeSummaryResponseModel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final Auth0ManagementTokenService managementTokenService;
    private final RestTemplate restTemplate;

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.roles.client}")
    private String clientRoleId;

    @Value("${auth0.roles.employee}")
    private String employeeRoleId;

    @Value("${auth0.roles.admin}")
    private String adminRoleId;

    @Override
    public Map<String, Object> getUsersByRole(String roleId, int page, int perPage) {
        String mgmtToken = managementTokenService.getManagementApiToken();

        String encodedRoleId = URLEncoder.encode(roleId, StandardCharsets.UTF_8);

        String url = String.format(
                "https://%s/api/v2/roles/%s/users?page=%d&per_page=%d&include_totals=true",
                domain,
                encodedRoleId,
                page,
                perPage
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to fetch users: " + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> users = (List<Map<String, Object>>) responseBody.getOrDefault("users", List.of());
        int total = responseBody.get("total") instanceof Number
                ? ((Number) responseBody.get("total")).intValue()
                : users.size();

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        result.put("page", page);
        result.put("perPage", perPage);

        return result;
    }

    @Override
    public Map<String, Object> searchUsers(String query, String roleName, int page, int perPage) {
        String mgmtToken = managementTokenService.getManagementApiToken();

        String formattedQuery = buildSearchQuery(query);

        String url = UriComponentsBuilder
                .fromHttpUrl(String.format("https://%s/api/v2/users", domain))
                .queryParam("q", formattedQuery)
                .queryParam("search_engine", "v3")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("include_totals", true)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to search users: " + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> users = (List<Map<String, Object>>) responseBody.getOrDefault("users", List.of());

        if (roleName != null && !roleName.trim().isEmpty()) {
            String roleId = getRoleIdFromName(roleName);
            if (roleId != null) {
                Set<String> roleUserIds = getUserIdsForRole(roleId, mgmtToken);
                users = users.stream()
                        .filter(user -> roleUserIds.contains(user.get("user_id")))
                        .collect(Collectors.toList());
            }
        }

        int total = users.size();

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        result.put("page", page);
        result.put("perPage", perPage);

        return result;
    }

    private String getRoleIdFromName(String roleName) {
        return switch (roleName.toLowerCase()) {
            case "clients", "client" -> clientRoleId;
            case "employees", "employee" -> employeeRoleId;
            case "admins", "admin" -> adminRoleId;
            default -> null;
        };
    }

    private Set<String> getUserIdsForRole(String roleId, String mgmtToken) {
        String encodedRoleId = URLEncoder.encode(roleId, StandardCharsets.UTF_8);

        String url = String.format(
                "https://%s/api/v2/roles/%s/users?per_page=100",
                domain,
                encodedRoleId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            Set<String> userIds = new HashSet<>();
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                for (Object userObj : response.getBody()) {
                    if (userObj instanceof Map) {
                        Map<String, Object> user = (Map<String, Object>) userObj;
                        userIds.add((String) user.get("user_id"));
                    }
                }
            }
            return userIds;
        } catch (Exception e) {
            log.error("Failed to fetch users for role: {}", roleId, e);
            return new HashSet<>();
        }
    }

    private String buildSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "*";
        }

        String sanitized = query.trim()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("&&", "\\&&")
                .replace("||", "\\||")
                .replace("!", "\\!")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("^", "\\^")
                .replace("~", "\\~")
                .replace("?", "\\?")
                .replace(":", "\\:");

        if (sanitized.length() < 3) {
            return String.format("email:%s* OR name:%s* OR user_id:%s*",
                    sanitized, sanitized, sanitized);
        }

        return String.format("email:*%s* OR name:*%s* OR user_id:*%s*",
                sanitized, sanitized, sanitized);
    }

    @Override
    public Map<String, Object> getUsersWithoutRole(String roleId, int page, int perPage) {
        String mgmtToken = managementTokenService.getManagementApiToken();

        String allUsersUrl = String.format(
                "https://%s/api/v2/users?page=%d&per_page=%d&include_totals=true",
                domain,
                page,
                perPage
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mgmtToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> allUsersResponse = restTemplate.exchange(
                allUsersUrl,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (!allUsersResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to fetch users: " + allUsersResponse.getStatusCode());
        }

        Map<String, Object> allUsersBody = allUsersResponse.getBody();
        List<Map<String, Object>> allUsers = (List<Map<String, Object>>) allUsersBody.getOrDefault("users", List.of());

        Set<String> roleUserIds = getUserIdsForRole(roleId, mgmtToken);

        List<Map<String, Object>> filteredUsers = allUsers.stream()
                .filter(user -> !roleUserIds.contains(user.get("user_id")))
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("users", filteredUsers);
        result.put("total", filteredUsers.size());
        result.put("page", page);
        result.put("perPage", perPage);

        return result;
    }

    @Override
    public List<EmployeeSummaryResponseModel> getAllEmployees(int page, int perPage) {
        Map<String, Object> raw = getUsersByRole(employeeRoleId, page, perPage);

        List<Map<String, Object>> users =
                (List<Map<String, Object>>) raw.getOrDefault("users", List.of());

        return users.stream()
                .map(u -> {
                    String userId = (String) u.get("user_id");
                    String name = (String) u.getOrDefault("name", u.get("nickname"));
                    String email = (String) u.get("email");
                    return new EmployeeSummaryResponseModel(userId, name, email);
                })
                .toList();
    }
}
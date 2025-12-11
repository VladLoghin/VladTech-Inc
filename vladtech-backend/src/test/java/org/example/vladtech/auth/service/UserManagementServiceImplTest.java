package org.example.vladtech.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private Auth0ManagementTokenService managementTokenService;

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private UserManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        service = new UserManagementServiceImpl(managementTokenService, restTemplate);

        ReflectionTestUtils.setField(service, "domain", "test-tenant.auth0.com");
        ReflectionTestUtils.setField(service, "clientRoleId", "client-role-id");
        ReflectionTestUtils.setField(service, "employeeRoleId", "employee-role-id");
        ReflectionTestUtils.setField(service, "adminRoleId", "admin-role-id");
    }

    @Test
    void getUsersByRole_returnsUsers_whenResponseIsSuccessful() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String expectedUrl = "https://test-tenant.auth0.com/api/v2/roles/role-123/users?page=0&per_page=25&include_totals=true";

        String json = """
        {
            "users": [
                {"user_id": "auth0|user1", "email": "user1@example.com"},
                {"user_id": "auth0|user2", "email": "user2@example.com"}
            ],
            "total": 2
        }
        """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.getUsersByRole("role-123", 0, 25);

        assertEquals(2, result.get("total"));
        assertEquals(0, result.get("page"));
        assertEquals(25, result.get("perPage"));

        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(2, users.size());
        assertEquals("auth0|user1", users.get(0).get("user_id"));

        mockServer.verify();
    }

    @Test
    void getUsersByRole_throwsException_whenApiReturnsError() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String expectedUrl = "https://test-tenant.auth0.com/api/v2/roles/role-123/users?page=0&per_page=25&include_totals=true";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(Exception.class,
                () -> service.getUsersByRole("role-123", 0, 25));

        mockServer.verify();
    }

    @Test
    void searchUsers_returnsMatchingUsers_whenQueryProvided() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String json = """
        {
            "users": [
                {"user_id": "auth0|found1", "email": "john@example.com"}
            ],
            "total": 1
        }
        """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:jo* OR name:jo* OR user_id:jo*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("jo", null, 0, 25);

        assertEquals(1, result.get("total"));
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(1, users.size());
        assertEquals("john@example.com", users.get(0).get("email"));

        mockServer.verify();
    }

    @Test
    void searchUsers_filtersUsersByRole_whenRoleProvided() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String searchJson = """
        {
            "users": [
                {"user_id": "auth0|user1", "email": "john@example.com"},
                {"user_id": "auth0|user2", "email": "jane@example.com"}
            ],
            "total": 2
        }
        """;

        String roleUsersJson = """
        [
            {"user_id": "auth0|user1"}
        ]
        """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*john* OR name:*john* OR user_id:*john*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchJson, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("https://test-tenant.auth0.com/api/v2/roles/client-role-id/users?per_page=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(roleUsersJson, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("john", "clients", 0, 25);

        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(1, users.size());
        assertEquals("auth0|user1", users.get(0).get("user_id"));

        mockServer.verify();
    }

    @Test
    void searchUsers_returnsAllUsers_whenRoleIsNull() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String json = """
        {
            "users": [
                {"user_id": "auth0|user1"},
                {"user_id": "auth0|user2"}
            ],
            "total": 2
        }
        """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*test* OR name:*test* OR user_id:*test*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("test", null, 0, 25);

        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(2, users.size());

        mockServer.verify();
    }

    @Test
    void searchUsers_returnsAllUsers_whenRoleIsEmpty() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String json = """
        {
            "users": [
                {"user_id": "auth0|user1"}
            ],
            "total": 1
        }
        """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*test* OR name:*test* OR user_id:*test*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("test", "", 0, 25);

        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(1, users.size());

        mockServer.verify();
    }

    @Test
    void searchUsers_throwsException_whenApiReturnsError() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*test* OR name:*test* OR user_id:*test*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(Exception.class,
                () -> service.searchUsers("test", null, 0, 25));

        mockServer.verify();
    }

    @Test
    void getUsersWithoutRole_returnsFilteredUsers_whenRoleUsersFetched() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String allUsersUrl = "https://test-tenant.auth0.com/api/v2/users?page=0&per_page=25&include_totals=true";
        String roleUsersUrl = "https://test-tenant.auth0.com/api/v2/roles/role-123/users?per_page=100";

        String allUsersJson = """
        {
            "users": [
                {"user_id": "auth0|user1", "email": "user1@example.com"},
                {"user_id": "auth0|user2", "email": "user2@example.com"},
                {"user_id": "auth0|user3", "email": "user3@example.com"}
            ]
        }
        """;

        String roleUsersJson = """
        [
            {"user_id": "auth0|user1"}
        ]
        """;

        mockServer.expect(requestTo(allUsersUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(allUsersJson, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(roleUsersUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(roleUsersJson, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.getUsersWithoutRole("role-123", 0, 25);

        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(2, users.size());
        assertEquals(2, result.get("total"));

        assertTrue(users.stream().noneMatch(u -> "auth0|user1".equals(u.get("user_id"))));
        assertTrue(users.stream().anyMatch(u -> "auth0|user2".equals(u.get("user_id"))));
        assertTrue(users.stream().anyMatch(u -> "auth0|user3".equals(u.get("user_id"))));

        mockServer.verify();
    }

    @Test
    void getUsersWithoutRole_throwsException_whenAllUsersFetchFails() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String allUsersUrl = "https://test-tenant.auth0.com/api/v2/users?page=0&per_page=25&include_totals=true";

        mockServer.expect(requestTo(allUsersUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(Exception.class,
                () -> service.getUsersWithoutRole("role-123", 0, 25));

        mockServer.verify();
    }

    @Test
    void getUsersByRole_handlesEmptyUsersList() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String expectedUrl = "https://test-tenant.auth0.com/api/v2/roles/role-123/users?page=0&per_page=25&include_totals=true";

        String json = """
        {
            "users": [],
            "total": 0
        }
        """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.getUsersByRole("role-123", 0, 25);

        assertEquals(0, result.get("total"));
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertTrue(users.isEmpty());

        mockServer.verify();
    }

    @Test
    void getRoleIdFromName_returnsEmployeeRoleId_forEmployeeVariants() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String searchJson = """
    {
        "users": [
            {"user_id": "auth0|user1", "email": "emp@example.com"}
        ],
        "total": 1
    }
    """;

        String roleUsersJson = """
    [
        {"user_id": "auth0|user1"}
    ]
    """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*test* OR name:*test* OR user_id:*test*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchJson, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("https://test-tenant.auth0.com/api/v2/roles/employee-role-id/users?per_page=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(roleUsersJson, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("test", "employees", 0, 25);

        assertEquals(1, ((List<?>) result.get("users")).size());
        mockServer.verify();
    }

    @Test
    void getRoleIdFromName_returnsAdminRoleId_forAdminVariants() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String searchJson = """
    {
        "users": [
            {"user_id": "auth0|admin1", "email": "admin@example.com"}
        ],
        "total": 1
    }
    """;

        String roleUsersJson = """
    [
        {"user_id": "auth0|admin1"}
    ]
    """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*test* OR name:*test* OR user_id:*test*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchJson, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("https://test-tenant.auth0.com/api/v2/roles/admin-role-id/users?per_page=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(roleUsersJson, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("test", "admin", 0, 25);

        assertEquals(1, ((List<?>) result.get("users")).size());
        mockServer.verify();
    }

    @Test
    void searchUsers_handlesRoleFetchException_gracefully() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String searchJson = """
    {
        "users": [
            {"user_id": "auth0|user1", "email": "john@example.com"}
        ],
        "total": 1
    }
    """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "email:*john* OR name:*john* OR user_id:*john*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchJson, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("https://test-tenant.auth0.com/api/v2/roles/client-role-id/users?per_page=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Map<String, Object> result = service.searchUsers("john", "client", 0, 25);

        assertEquals(0, result.get("total"));
        mockServer.verify();
    }

    @Test
    void buildSearchQuery_returnsWildcard_whenQueryIsNull() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String json = """
    {
        "users": [
            {"user_id": "auth0|user1"}
        ],
        "total": 1
    }
    """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers(null, null, 0, 25);

        assertEquals(1, ((List<?>) result.get("users")).size());
        mockServer.verify();
    }

    @Test
    void buildSearchQuery_returnsWildcard_whenQueryIsEmpty() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String json = """
    {
        "users": [
            {"user_id": "auth0|user1"}
        ],
        "total": 1
    }
    """;

        mockServer.expect(requestToUriTemplate(
                        "https://test-tenant.auth0.com/api/v2/users?q={q}&search_engine={engine}&page={page}&per_page={perPage}&include_totals={totals}",
                        "*", "v3", "0", "25", "true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.searchUsers("   ", null, 0, 25);

        assertEquals(1, ((List<?>) result.get("users")).size());
        mockServer.verify();
    }
}
package org.example.vladtech.auth.service;

import org.example.vladtech.auth.dataaccess.UserProfile;
import org.example.vladtech.auth.dataaccess.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private Auth0ManagementTokenService managementTokenService;

    @Mock
    private UserProfileRepository userProfileRepository;

    // We want MockRestServiceServer, so RestTemplate must be a REAL instance.
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    private UserManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        // ✅ real RestTemplate for MockRestServiceServer
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        // ✅ inject mocks + real restTemplate
        service = new UserManagementServiceImpl(managementTokenService, restTemplate, userProfileRepository);

        ReflectionTestUtils.setField(service, "domain", "test-tenant.auth0.com");
        ReflectionTestUtils.setField(service, "clientRoleId", "client-role-id");
        ReflectionTestUtils.setField(service, "employeeRoleId", "employee-role-id");
        ReflectionTestUtils.setField(service, "adminRoleId", "admin-role-id");
    }

    @Test
    void getUsersByRole_returnsUsers_whenResponseIsSuccessful() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

        String expectedUrl =
                "https://test-tenant.auth0.com/api/v2/roles/role-123/users?page=0&per_page=25&include_totals=true";

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

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(2, users.size());
        assertEquals("auth0|user1", users.get(0).get("user_id"));

        mockServer.verify();
    }

    @Test
    void getUsersByRole_throwsException_whenApiReturnsError() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

        String expectedUrl =
                "https://test-tenant.auth0.com/api/v2/roles/role-123/users?page=0&per_page=25&include_totals=true";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(Exception.class, () -> service.getUsersByRole("role-123", 0, 25));

        mockServer.verify();
    }

    @Test
    void searchUsers_returnsMatchingUsers_whenQueryProvided() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

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
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(1, users.size());
        assertEquals("john@example.com", users.get(0).get("email"));

        mockServer.verify();
    }

    @Test
    void searchUsers_filtersUsersByRole_whenRoleProvided() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

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

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");
        assertEquals(1, users.size());
        assertEquals("auth0|user1", users.get(0).get("user_id"));

        mockServer.verify();
    }

    @Test
    void searchUsers_handlesRoleFetchException_gracefully() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

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

    // ============================
    // Repository-backed unit tests
    // ============================

    @Test
    void syncUserProfile_updatesNickname_whenExistingProfileHasNoNickname() {
        String userId = "user123";
        String email = "user123@example.com";
        String name = "Updated Name";

        UserProfile existingProfile = UserProfile.builder()
                .auth0Sub(userId)
                .email(email)
                .nickname(null)
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));

        service.syncUserProfile(userId, email, name);

        assertEquals(name, existingProfile.getNickname());
        verify(userProfileRepository).save(existingProfile);
    }

    @Test
    void syncUserProfile_doesNothing_whenExistingProfileHasNickname() {
        String userId = "user123";
        String email = "user123@example.com";
        String name = "Updated Name";

        UserProfile existingProfile = UserProfile.builder()
                .auth0Sub(userId)
                .email(email)
                .nickname("Existing Nickname")
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));

        service.syncUserProfile(userId, email, name);

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void syncUserProfile_createsNewProfile_whenNoExistingProfile() {
        String userId = "user123";
        String email = "user123@example.com";
        String name = "New Nickname";

        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        service.syncUserProfile(userId, email, name);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());

        UserProfile saved = captor.getValue();
        assertEquals(userId, saved.getAuth0Sub());
        assertEquals(email, saved.getEmail());
        assertEquals(name, saved.getNickname());
    }

    @Test
    void getUserNameById_returnsNickname_whenUserExists() {
        String userId = "user123";
        String nickname = "Test User";

        UserProfile profile = UserProfile.builder()
                .auth0Sub(userId)
                .nickname(nickname)
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        String result = service.getUserNameById(userId);

        assertEquals(nickname, result);
    }

    @Test
    void getUserNameById_returnsNull_whenUserDoesNotExist() {
        when(userProfileRepository.findById("user123")).thenReturn(Optional.empty());

        String result = service.getUserNameById("user123");

        assertNull(result);
    }

    @Test
    void updateUserName_updatesExistingProfile_whenUserExists() {
        String userId = "user123";
        String newName = "Updated Name";

        UserProfile existingProfile = UserProfile.builder()
                .auth0Sub(userId)
                .nickname("Old Name")
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));

        String result = service.updateUserName(userId, newName);

        assertEquals(newName, existingProfile.getNickname());
        verify(userProfileRepository).save(existingProfile);
        assertEquals("User name updated successfully.", result);
    }

    @Test
    void updateUserName_createsNewProfile_whenUserDoesNotExist() {
        String userId = "user123";
        String newName = "New Name";

        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        String result = service.updateUserName(userId, newName);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());

        UserProfile savedProfile = captor.getValue();
        assertEquals(userId, savedProfile.getAuth0Sub());
        assertEquals(newName, savedProfile.getNickname());
        assertEquals("User name updated successfully.", result);
    }

    @Test
    void getUsersWithoutRole_returnsFilteredUsers_whenRoleUsersFetched() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

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
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andRespond(withSuccess(allUsersJson, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(roleUsersUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andRespond(withSuccess(roleUsersJson, MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.getUsersWithoutRole("role-123", 0, 25);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("users");

        assertEquals(2, users.size());
        assertEquals(2, result.get("total"));
        assertEquals(0, result.get("page"));
        assertEquals(25, result.get("perPage"));

        assertTrue(users.stream().noneMatch(u -> "auth0|user1".equals(u.get("user_id"))));
        assertTrue(users.stream().anyMatch(u -> "auth0|user2".equals(u.get("user_id"))));
        assertTrue(users.stream().anyMatch(u -> "auth0|user3".equals(u.get("user_id"))));

        mockServer.verify();
    }

    @Test
    void getUsersWithoutRole_throwsHttpServerError_whenAllUsersFetchFails() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

        String allUsersUrl = "https://test-tenant.auth0.com/api/v2/users?page=0&per_page=25&include_totals=true";

        mockServer.expect(requestTo(allUsersUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class,
                () -> service.getUsersWithoutRole("role-123", 0, 25));

        mockServer.verify();
    }


    @Test
    void getAllEmployees_returnsMappedEmployees_prefersNameThenNickname() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

        String expectedUrl =
                "https://test-tenant.auth0.com/api/v2/roles/employee-role-id/users?page=0&per_page=25&include_totals=true";

        String json = """
            {
              "users": [
                {"user_id": "auth0|emp1", "email": "emp1@example.com", "name": "Employee One"},
                {"user_id": "auth0|emp2", "email": "emp2@example.com", "nickname": "Emp Two Nick"}
              ],
              "total": 2
            }
            """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        var result = service.getAllEmployees(0, 25);

        assertEquals(2, result.size());

        assertEquals("auth0|emp1", result.get(0).userId());
        assertEquals("Employee One", result.get(0).name());
        assertEquals("emp1@example.com", result.get(0).email());

        assertEquals("auth0|emp2", result.get(1).userId());
        assertEquals("Emp Two Nick", result.get(1).name()); // fell back to nickname
        assertEquals("emp2@example.com", result.get(1).email());

        mockServer.verify();
    }

    @Test
    void getUserEmailById_returnsEmail_whenUserExists() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

        String userId = "auth0|user123";

        String json = """
            {
              "user_id": "auth0|user123",
              "email": "user123@example.com",
              "name": "Test User"
            }
            """;

        // IMPORTANT: because your service uses the template URL with {userId},
        // use requestToUriTemplate.
        mockServer.expect(requestToUriTemplate("https://test-tenant.auth0.com/api/v2/users/{userId}", userId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        String result = service.getUserEmailById(userId);

        assertEquals("user123@example.com", result);
        mockServer.verify();
    }

    @Test
    void getUserEmailById_returnsNull_whenUserIdIsBlank() {
        assertNull(service.getUserEmailById("   "));
    }

    @Test
    void getUserEmailById_returnsNull_whenUserIdIsNull() {
        assertNull(service.getUserEmailById(null));
    }

    @Test
    void getUserEmailById_throwsHttpServerError_whenApiReturns500() {
        when(managementTokenService.getManagementApiToken()).thenReturn("fake-mgmt-token");

        String userId = "auth0|user123";

        mockServer.expect(requestToUriTemplate("https://test-tenant.auth0.com/api/v2/users/{userId}", userId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> service.getUserEmailById(userId));

        mockServer.verify();
    }

}

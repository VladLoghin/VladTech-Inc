package org.example.vladtech.auth.service;

import org.example.vladtech.auth.dataaccess.RoleChangeLogRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceImplTest {

        @Mock
        private Auth0ManagementTokenService managementTokenService;

        @Mock
        private RoleChangeLogRepository roleChangeLogRepository;

        private RestTemplate restTemplate;
        private MockRestServiceServer mockServer;

        private RoleAssignmentServiceImpl service;

        @BeforeEach
        void setUp() {
                restTemplate = new RestTemplate();
                mockServer = MockRestServiceServer.bindTo(restTemplate).build();

                service = new RoleAssignmentServiceImpl(
                                managementTokenService,
                                restTemplate,
                                roleChangeLogRepository);

                // Inject @Value fields manually
                ReflectionTestUtils.setField(service, "domain", "dev-ljz84r2xvrlnftfv.ca.auth0.com");
                ReflectionTestUtils.setField(service, "clientRoleId", "client-role-id");
                ReflectionTestUtils.setField(service, "employeeRoleId", "employee-role-id");
                ReflectionTestUtils.setField(service, "adminRoleId", "admin-role-id");
        }

        @Test
        void assignClientRole_success() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/client-role-id/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andExpect(jsonPath("$.users[0]").value("user123"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.assignClientRole("user123", "Test User");

                mockServer.verify();
        }

        @Test
        void assignClientRole_throwsException_whenApiReturnsError() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/client-role-id/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

                Assertions.assertThrows(
                                HttpServerErrorException.InternalServerError.class,
                                () -> service.assignClientRole("user123", null));

                mockServer.verify();
        }

        @Test
        void assignEmployeeRole_success() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/employee-role-id/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andExpect(jsonPath("$.users[0]").value("user456"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.assignEmployeeRole("user456", "Test User");

                mockServer.verify();
        }

        @Test
        void assignEmployeeRole_throwsException_whenApiReturnsError() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/employee-role-id/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

                Assertions.assertThrows(
                                HttpServerErrorException.InternalServerError.class,
                                () -> service.assignEmployeeRole("user456", null));

                mockServer.verify();
        }

        @Test
        void assignAdminRole_success() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/admin-role-id/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andExpect(jsonPath("$.users[0]").value("user789"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.assignAdminRole("user789", "Test User");

                mockServer.verify();
        }

        @Test
        void assignAdminRole_throwsException_whenApiReturnsError() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/admin-role-id/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

                Assertions.assertThrows(
                                HttpServerErrorException.InternalServerError.class,
                                () -> service.assignAdminRole("user789", null));

                mockServer.verify();
        }

        @Test
        void assignRole_success_withCustomRoleId() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String customRoleId = "custom-role-xyz";
                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/" + customRoleId
                                + "/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andExpect(jsonPath("$.users[0]").value("userABC"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.assignRole("userABC", customRoleId);

                mockServer.verify();
        }

        @Test
        void assignRole_throwsException_whenApiReturnsError() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String customRoleId = "custom-role-xyz";
                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/" + customRoleId
                                + "/users";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.POST))
                                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

                Assertions.assertThrows(
                                HttpServerErrorException.InternalServerError.class,
                                () -> service.assignRole("userABC", customRoleId));

                mockServer.verify();
        }

        // Role removal tests

        @Test
        void removeClientRole_success() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/users/user123/roles";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.DELETE))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.removeClientRole("user123", "Test User");

                mockServer.verify();
        }

        @Test
        void removeEmployeeRole_success() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/users/user456/roles";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.DELETE))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.removeEmployeeRole("user456", "Test User");

                mockServer.verify();
        }

        @Test
        void removeAdminRole_success() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/users/user789/roles";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.DELETE))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.removeAdminRole("user789", "Test User");

                mockServer.verify();
        }

        @Test
        void removeRole_success_withCustomRoleId() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/users/userXYZ/roles";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.DELETE))
                                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                                .andRespond(withStatus(HttpStatus.OK));

                service.removeRole("userXYZ", "custom-role-xyz");

                mockServer.verify();
        }

        @Test
        void removeRole_throwsException_whenApiReturnsError() {
                when(managementTokenService.getManagementApiToken())
                                .thenReturn("fake-mgmt-token");

                String expectedUrl = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/users/userXYZ/roles";

                mockServer.expect(requestTo(expectedUrl))
                                .andExpect(method(HttpMethod.DELETE))
                                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

                Assertions.assertThrows(
                                HttpServerErrorException.InternalServerError.class,
                                () -> service.removeRole("userXYZ", "custom-role-xyz"));

                mockServer.verify();
        }

        @Test
        void getChangeLog_returnsAllLogsOrderedByDate() {
                // Arrange
                org.example.vladtech.auth.dataaccess.RoleChangeLog log1 = org.example.vladtech.auth.dataaccess.RoleChangeLog
                                .builder()
                                .userId("user1")
                                .userName("User One")
                                .roleName("Client")
                                .action("ASSIGNED")
                                .performedAt(java.time.Instant.now())
                                .build();
                org.example.vladtech.auth.dataaccess.RoleChangeLog log2 = org.example.vladtech.auth.dataaccess.RoleChangeLog
                                .builder()
                                .userId("user2")
                                .userName("User Two")
                                .roleName("Employee")
                                .action("REMOVED")
                                .performedAt(java.time.Instant.now().minusSeconds(3600))
                                .build();

                when(roleChangeLogRepository.findAllByOrderByPerformedAtDesc())
                                .thenReturn(java.util.List.of(log1, log2));

                // Act
                java.util.List<org.example.vladtech.auth.dataaccess.RoleChangeLog> result = service.getChangeLog();

                // Assert
                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals("user1", result.get(0).getUserId());
                Assertions.assertEquals("user2", result.get(1).getUserId());
        }

        @Test
        void getChangeLog_withPagination_returnsPagedResults() {
                // Arrange
                org.example.vladtech.auth.dataaccess.RoleChangeLog log1 = org.example.vladtech.auth.dataaccess.RoleChangeLog
                                .builder()
                                .userId("user1")
                                .userName("User One")
                                .roleName("Admin")
                                .action("ASSIGNED")
                                .performedAt(java.time.Instant.now())
                                .build();

                org.springframework.data.domain.Page<org.example.vladtech.auth.dataaccess.RoleChangeLog> page = new org.springframework.data.domain.PageImpl<>(
                                java.util.List.of(log1),
                                org.springframework.data.domain.PageRequest.of(0, 10),
                                1);

                when(roleChangeLogRepository.findAllByOrderByPerformedAtDesc(
                                org.springframework.data.domain.PageRequest.of(0, 10)))
                                .thenReturn(page);

                // Act
                org.springframework.data.domain.Page<org.example.vladtech.auth.dataaccess.RoleChangeLog> result = service
                                .getChangeLog(0, 10);

                // Assert
                Assertions.assertEquals(1, result.getTotalElements());
                Assertions.assertEquals("user1", result.getContent().get(0).getUserId());
        }
}

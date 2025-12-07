package org.example.vladtech.auth.service;

import org.junit.jupiter.api.Assertions;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceImplTest {

    @Mock
    private Auth0ManagementTokenService managementTokenService;

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    private RoleAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        service = new RoleAssignmentServiceImpl(
                managementTokenService,
                restTemplate
        );

        // Inject @Value fields manually
        ReflectionTestUtils.setField(service, "domain", "dev-ljz84r2xvrlnftfv.ca.auth0.com");
        ReflectionTestUtils.setField(service, "clientRoleId", "client-role-id");
    }

    @Test
    void assignClientRole_success() {
        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String expectedUrl =
                "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/client-role-id/users";

        String expectedBody = """
        {"users":["user123"]}
        """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer fake-mgmt-token"))
                .andExpect(jsonPath("$.users[0]").value("user123"))
                .andRespond(withStatus(HttpStatus.OK));

        service.assignClientRole("user123");

        mockServer.verify();
    }

    @Test
    void assignClientRole_throwsException_whenApiReturnsError() {

        when(managementTokenService.getManagementApiToken())
                .thenReturn("fake-mgmt-token");

        String expectedUrl =
                "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/api/v2/roles/client-role-id/users";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Expect Spring's HttpServerErrorException instead of IllegalStateException
        Assertions.assertThrows(
                HttpServerErrorException.InternalServerError.class,
                () -> service.assignClientRole("user123")
        );

        mockServer.verify();
    }
}

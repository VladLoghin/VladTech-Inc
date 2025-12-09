package org.example.vladtech.auth.service;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class Auth0ManagementTokenServiceImplTest {

    private Auth0ManagementTokenServiceImpl service;
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {

        restTemplate = new RestTemplate();


        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        service = new Auth0ManagementTokenServiceImpl(restTemplate);

        // Inject @Value fields manually
        ReflectionTestUtils.setField(service, "domain", "dev-ljz84r2xvrlnftfv.ca.auth0.com");
        ReflectionTestUtils.setField(service, "clientId", "my-client-id");
        ReflectionTestUtils.setField(service, "clientSecret", "my-client-secret");
        ReflectionTestUtils.setField(service, "audience", "my-audience");
    }


    @Test
    void getManagementApiToken_returnsToken_whenResponseIsSuccessful() {

        String url = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/oauth/token";

        String json = """
        {
            "access_token": "mocked-access-token"
        }
        """;

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        String token = service.getManagementApiToken();

        assertEquals("mocked-access-token", token);
        mockServer.verify();
    }

    @Test
    void getManagementApiToken_throwsException_whenResponseStatusIsNot2xx() {
        String url = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        String responseBody = "Bad Request Error";

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw HttpClientErrorException.create(
                            HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            headers,
                            responseBody.getBytes(),
                            null
                    );
                });

        HttpClientErrorException.BadRequest exception = assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            service.getManagementApiToken();
        });

        assertTrue(exception.getMessage().contains("400 Bad Request"));
        mockServer.verify();
    }

    @Test
    void getManagementApiToken_throwsException_whenAccessTokenIsMissing() {
        String url = "https://dev-ljz84r2xvrlnftfv.ca.auth0.com/oauth/token";

        String json = """
    {
        "other_field": "value"
    }
    """;

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.getManagementApiToken();
        });

        assertTrue(exception.getMessage().contains("No access_token in management token response"));
        mockServer.verify();
    }

}



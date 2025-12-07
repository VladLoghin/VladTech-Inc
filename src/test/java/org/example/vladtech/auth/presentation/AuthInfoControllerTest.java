package org.example.vladtech.auth.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthInfoControllerTest {

    private AuthInfoController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthInfoController();
    }

    @Test
    void getUserInfo_returnsUserFields_whenJwtIsPresent() {

        Jwt jwt = new Jwt(
                "fake-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "auth0|123",
                        "email", "vlad@example.com",
                        "https://vladtech.com/roles", List.of("Admin", "Employee, Client")
                )
        );

        Map<String, Object> result = controller.me(jwt);

        assertTrue((Boolean) result.get("authenticated"));
        assertEquals("auth0|123", result.get("sub"));
        assertEquals("vlad@example.com", result.get("email"));
        assertEquals(List.of("Admin", "Employee, Client"), result.get("roles"));
    }

    @Test
    void getUserInfo_returnsUnauthenticated_whenJwtIsNull() {
        Map<String, Object> result = controller.me(null);

        assertFalse((Boolean) result.get("authenticated"));
        assertNull(result.get("sub"));
        assertNull(result.get("email"));
        assertNull(result.get("roles"));
    }
}
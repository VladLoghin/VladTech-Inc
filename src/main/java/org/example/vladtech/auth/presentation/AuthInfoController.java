package org.example.vladtech.auth.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/authinfo")
public class AuthInfoController {

    //can be used for debug
        @GetMapping("/me")
        public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
            return Map.of(
                    "subject", jwt.getClaim("sub"),
                    "email", jwt.getClaim("email"),
                    "roles", jwt.getClaim("https://vladtech.com/roles")
            );
        }
    }



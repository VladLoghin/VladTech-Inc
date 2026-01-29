package org.example.vladtech.auth.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.example.vladtech.auth.service.UserManagementServiceImpl;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserManagementServiceImpl userManagementService;

    @GetMapping("/hello")
    public String helloPublic() {
        return "Hello, Public User!";
    }

    @PostMapping("/sync-profile")
    public ResponseEntity<String> syncProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String name = jwt.getClaim("name");
        
        // Default to email username or "User" if name is null
        if (name == null || name.isBlank()) {
            name = email != null ? email.split("@")[0] : "User";
        }
        
        userManagementService.syncUserProfile(userId, email, name);
        return ResponseEntity.ok("Profile synced");
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String userName = userManagementService.getUserNameById(userId);
        
        // Return default if nickname is null
        if (userName == null || userName.isBlank()) {
            String email = jwt.getClaim("email");
            userName = email != null ? email.split("@")[0] : "User";
        }
        
        return ResponseEntity.ok("User Name: " + userName);
    }

    @PatchMapping("/profile/update-name")
    public ResponseEntity<String> updateUserName(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> payload
    ) {
        try {
            String userId = jwt.getSubject();
            String newName = payload.get("name");

            if (newName == null || newName.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid name.");
            }

            String result = userManagementService.updateUserName(userId, newName);
            return ResponseEntity.ok(result);
            
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found in Auth0");
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("Auth0 error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }
}

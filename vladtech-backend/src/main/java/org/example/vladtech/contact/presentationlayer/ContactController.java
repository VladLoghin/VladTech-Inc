package org.example.vladtech.contact.presentationlayer;

import org.example.vladtech.contact.businesslayer.ContactService;
import org.example.vladtech.auth.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;
    private final UserManagementService userManagementService;

    public ContactController(ContactService contactService, UserManagementService userManagementService) {
        this.contactService = contactService;
        this.userManagementService = userManagementService;
    }

    @PreAuthorize("hasAnyAuthority('Admin', 'Client', 'Employee')")
    @PostMapping
    public ResponseEntity<Void> sendContact(
            @RequestBody ContactRequestDto requestDto,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String name = userManagementService.getUserNameById(userId);
        
        // Default to email prefix if name is null
        if (name == null || name.isBlank()) {
            name = email != null ? email.split("@")[0] : "User";
        }

        contactService.sendContactMessage(requestDto, name, email);

        return ResponseEntity.ok().build();
    }

}

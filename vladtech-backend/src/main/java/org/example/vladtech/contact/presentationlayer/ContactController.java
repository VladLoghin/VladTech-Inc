package org.example.vladtech.contact.presentationlayer;

import org.example.vladtech.contact.businesslayer.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }
    @PreAuthorize("hasAnyAuthority('Admin', 'Client', 'Employee')")
    @PostMapping
    public ResponseEntity<Void> sendContact(@RequestBody ContactRequestDto requestDto, Jwt jwt) {

        contactService.sendContactMessage(requestDto, jwt);

        return ResponseEntity.ok().build();
    }
}

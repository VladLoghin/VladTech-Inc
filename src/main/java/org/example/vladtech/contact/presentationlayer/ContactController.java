package org.example.vladtech.contact.presentationlayer;

import org.example.vladtech.contact.businesslayer.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Void> sendContact(@RequestBody ContactRequestDto requestDto) {

        contactService.sendContactMessage(requestDto);

        // 200 OK, no body
        return ResponseEntity.ok().build();
    }
}

package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ContactService {

    void sendContactMessage(ContactRequestDto requestDto, Jwt jwt);
}

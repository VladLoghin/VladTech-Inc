package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.datamapperlayer.ContactEmailMapper;
import org.example.vladtech.contact.dataaccesslayer.ContactEmailSender;
import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactEmailMapper mapper;
    private final ContactEmailSender emailSender;

    public ContactServiceImpl(ContactEmailMapper mapper,
                              ContactEmailSender emailSender) {
        this.mapper = mapper;
        this.emailSender = emailSender;
    }

    @Override
    public void sendContactMessage(ContactRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getClaimAsString("email");
        String name  = jwt.getClaimAsString("name");
        if (name == null) name = jwt.getClaimAsString("nickname");
        if (name == null) name = email;

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("No 'email' claim found in JWT access token.");
        }

        ContactEmail contactEmail = mapper.toContactEmail(requestDto, name, email);
        emailSender.send(contactEmail);
    }

}

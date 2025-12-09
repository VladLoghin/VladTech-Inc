package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.datamapperlayer.ContactEmailMapper;
import org.example.vladtech.contact.dataaccesslayer.ContactEmailSender;
import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
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
    public void sendContactMessage(ContactRequestDto requestDto) {
        // 1. Convert DTO (what frontend sends) → domain object
        ContactEmail contactEmail = mapper.toContactEmail(requestDto);

        // 2. Ask data access layer to send the email (MailHog)
        emailSender.send(contactEmail);
    }
}

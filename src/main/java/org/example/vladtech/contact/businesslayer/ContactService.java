package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.presentationlayer.ContactRequestDto;

public interface ContactService {

    void sendContactMessage(ContactRequestDto requestDto);
}

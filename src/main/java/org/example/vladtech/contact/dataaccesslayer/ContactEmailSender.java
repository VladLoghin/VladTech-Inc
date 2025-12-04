package org.example.vladtech.contact.dataaccesslayer;

import org.example.vladtech.contact.domain.ContactEmail;

public interface ContactEmailSender {
    void send(ContactEmail email);
}

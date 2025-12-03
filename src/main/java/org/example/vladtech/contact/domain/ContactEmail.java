package org.example.vladtech.contact.domain;

import java.time.LocalDateTime;

public class ContactEmail {

    private String destinary;      // who receives the email (admin address)
    private String title;          // subject
    private String templateName;   // example "CONTACT_US"
    private String header;
    private String body;
    private String footer;
    private String senderName;
    private String senderEmail;    // client's email address
    private LocalDateTime sentDate;

    public ContactEmail(String destinary,
                        String title,
                        String templateName,
                        String header,
                        String body,
                        String footer,
                        String senderName,
                        String senderEmail,
                        LocalDateTime sentDate) {
        this.destinary = destinary;
        this.title = title;
        this.templateName = templateName;
        this.header = header;
        this.body = body;
        this.footer = footer;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.sentDate = sentDate;
    }

    public String getDestinary() {
        return destinary;
    }

    public String getTitle() {
        return title;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getFooter() {
        return footer;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }
}

package org.example.vladtech.contact.datamapperlayer;

import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;

import java.time.LocalDateTime;

public class ContactEmailMapper {

    // For now we hardcode the admin address and template name.
    // Later we can move these to application.yml if needed.
    private static final String ADMIN_EMAIL = "admin@vladtech.com";
    private static final String TEMPLATE_NAME = "CONTACT_US";

    // Map the incoming DTO from the frontend to our domain object
    public ContactEmail toContactEmail(ContactRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("requestDto cannot be null");
        }

        String header = "New contact request from " + safe(requestDto.getName());
        String body = requestDto.getMessage();
        String footer = "Reply to: " + safe(requestDto.getEmail());

        return new ContactEmail(
                ADMIN_EMAIL,                        // destinary  admin inbox
                requestDto.getSubject(),            // title
                TEMPLATE_NAME,                      // templateName
                header,
                body,
                footer,
                requestDto.getName(),               // senderName
                LocalDateTime.now()                 // sentDate
        );
    }

    // Build a simple HTML version of the email
    public String toHtml(ContactEmail email) {
        if (email == null) {
            throw new IllegalArgumentException("email cannot be null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>").append(escape(email.getHeader())).append("</h2>");
        sb.append("<p>").append(escape(email.getBody())).append("</p>");
        sb.append("<hr/>");
        sb.append("<p>").append(escape(email.getFooter())).append("</p>");
        sb.append("<p>Sent by ")
                .append(escape(email.getSenderName()))
                .append(" at ")
                .append(email.getSentDate())
                .append("</p>");
        sb.append("</body></html>");

        return sb.toString();
    }

    // Small helpers so we do not explode on nulls and so on
    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        // Very basic escaping for now
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

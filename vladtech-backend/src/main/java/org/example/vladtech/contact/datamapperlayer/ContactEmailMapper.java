package org.example.vladtech.contact.datamapperlayer;

import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ContactEmailMapper {

    private final String adminEmail;
    private static final String TEMPLATE_NAME = "CONTACT_US";

    public ContactEmailMapper(@Value("${email.admin}") String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public ContactEmail toContactEmail(ContactRequestDto requestDto, String senderName, String senderEmail) {
        if (requestDto == null) {
            throw new IllegalArgumentException("requestDto cannot be null");
        }

        String header = "New contact request from " + safe(senderName);
        String body = safe(requestDto.getMessage());
        String footer = "Reply to: " + safe(senderEmail);

        return new ContactEmail(
                adminEmail,
                safe(requestDto.getSubject()),
                TEMPLATE_NAME,
                header,
                body,
                footer,
                safe(senderName),
                safe(senderEmail),
                LocalDateTime.now(),
                safe(requestDto.getName())
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

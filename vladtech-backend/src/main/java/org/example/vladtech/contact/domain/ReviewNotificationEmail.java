package org.example.vladtech.contact.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ReviewNotificationEmail {
    private String destinary;
    private String title;
    private String templateName;
    private String header;
    private String body;
    private String footer;
    private String clientEmail;
    private LocalDateTime sentDate;
    private String senderName;
}

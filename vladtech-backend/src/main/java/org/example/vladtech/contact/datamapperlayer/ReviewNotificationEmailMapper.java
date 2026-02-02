package org.example.vladtech.contact.datamapperlayer;

import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.reviews.data.Review;
import org.example.vladtech.reviews.presentation.ReviewRequestModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReviewNotificationEmailMapper {

    private final String adminEmail;
    private static final String TEMPLATE_NAME = "REVIEW_NOTIFICATION";

    public ReviewNotificationEmailMapper(@Value("${email.admin}") String adminEmail) {
        this.adminEmail = adminEmail;
    }

    // No senderName/senderEmail needed if you want pure no-reply system mail
    public ContactEmail toNotificationEmail(ReviewRequestModel review) {
        if (review == null) throw new IllegalArgumentException("review cannot be null");

        String header = "New review submitted (pending approval)";
        String body =
                        "Client: " + safe(review.getClientName()) + "<br/>" +
                        "Rating: " + safe(String.valueOf(review.getRating())) + "<br/>" +
                        "Comment: " + safe(review.getComment());

        String footer = "Log in as Admin to review and approve it.";

        return new ContactEmail(
                adminEmail,
                "New review pending approval: " ,
                TEMPLATE_NAME,
                header,
                body,
                footer,
                "",                 // senderName (not used)
                "",                 // senderEmail / clientEmail => keeps Reply-To empty
                LocalDateTime.now(),
                safe(review.getClientName())
        );
    }

    private String safe(String v) { return v == null ? "" : v; }
}

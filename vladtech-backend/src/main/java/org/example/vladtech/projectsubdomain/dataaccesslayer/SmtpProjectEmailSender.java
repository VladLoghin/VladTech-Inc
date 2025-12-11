package org.example.vladtech.projectsubdomain.dataaccesslayer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;

@Repository
public class SmtpProjectEmailSender implements ProjectEmailSender {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final JavaMailSender mailSender;
    private final String noReplyEmail;

    public SmtpProjectEmailSender(JavaMailSender mailSender,
                                  @Value("${email.noreply}") String noReplyEmail) {
        this.mailSender = mailSender;
        this.noReplyEmail = noReplyEmail;
    }

    @Override
    public void send(ProjectNotificationEmail email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(noReplyEmail);
            helper.setTo(email.getRecipientEmail());
            helper.setSubject(email.getSubject());
            helper.setText(buildHtmlBody(email), true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send project notification email", e);
        }
    }

    private String buildHtmlBody(ProjectNotificationEmail email) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");

        sb.append("<h2 style='color: #333;'>Project ").append(email.getOperation()).append("</h2>");

        sb.append("<p>Dear ").append(escape(email.getClientName())).append(",</p>");

        sb.append("<p>Your project has been successfully ").append(email.getOperation().toLowerCase()).append(".</p>");

        sb.append("<div style='background-color: #f5f5f5; padding: 20px; border-radius: 5px; margin: 20px 0;'>");
        sb.append("<h3 style='margin-top: 0; color: #555;'>Project Details</h3>");

        sb.append("<p><strong>Project ID:</strong> ").append(escape(email.getProjectIdentifier())).append("</p>");
        sb.append("<p><strong>Project Name:</strong> ").append(escape(email.getProjectName())).append("</p>");

        if (email.getDescription() != null && !email.getDescription().isBlank()) {
            sb.append("<p><strong>Description:</strong> ").append(escape(email.getDescription())).append("</p>");
        }

        if (email.getProjectType() != null) {
            sb.append("<p><strong>Type:</strong> ").append(escape(email.getProjectType())).append("</p>");
        }

        if (email.getStartDate() != null) {
            sb.append("<p><strong>Start Date:</strong> ").append(email.getStartDate().format(DATE_FORMATTER)).append("</p>");
        }

        if (email.getDueDate() != null) {
            sb.append("<p><strong>Due Date:</strong> ").append(email.getDueDate().format(DATE_FORMATTER)).append("</p>");
        }

        if (email.getAddress() != null && !email.getAddress().isBlank()) {
            sb.append("<p><strong>Location:</strong> ").append(escape(email.getAddress())).append("</p>");
        }

        sb.append("</div>");

        sb.append("<p style='color: #666; font-size: 0.9em;'>This is an automated message. Please do not reply to this email.</p>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
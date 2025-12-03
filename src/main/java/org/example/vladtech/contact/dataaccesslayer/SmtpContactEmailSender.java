package org.example.vladtech.contact.dataaccesslayer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.vladtech.contact.domain.ContactEmail;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;

@Repository
public class SmtpContactEmailSender implements ContactEmailSender {

    private final JavaMailSender mailSender;

    public SmtpContactEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(ContactEmail email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setTo(email.getDestinary());
            helper.setSubject(email.getTitle());

            // Use the client's email as the From address if available
            String fromAddress = email.getSenderEmail();
            if (fromAddress == null || fromAddress.isBlank()) {
                fromAddress = "no-reply@vladtech.com";
            }
            helper.setFrom(fromAddress);

            helper.setText(buildHtmlBody(email), true); // true = HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // For now just wrap in RuntimeException; later you can log properly
            throw new RuntimeException("Failed to send contact email", e);
        }
    }

    private String buildHtmlBody(ContactEmail email) {
        // We reuse the structure you already defined in the mapper,
        // but here we rebuild it quickly so this class is self-contained.
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>").append(email.getHeader()).append("</h2>");
        sb.append("<p>").append(email.getBody()).append("</p>");
        sb.append("<hr/>");
        sb.append("<p>").append(email.getFooter()).append("</p>");
        sb.append("<p>Sent by ")
                .append(email.getSenderName())
                .append(" at ")
                .append(email.getSentDate())
                .append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}

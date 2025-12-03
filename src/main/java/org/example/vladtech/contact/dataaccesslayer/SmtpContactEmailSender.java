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

            String fromAddress =
                    (email.getClientEmail() != null && !email.getClientEmail().isBlank())
                            ? email.getClientEmail()
                            : "admin@vladtech.com";
            helper.setFrom(fromAddress);

            // Reply-To: the client's email address
            if (email.getClientEmail() != null && !email.getClientEmail().isBlank()) {
                helper.setReplyTo(email.getClientEmail());
            }

            helper.setTo(email.getDestinary());
            helper.setSubject(email.getTitle());
            helper.setText(buildHtmlBody(email), true); // true = HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send contact email", e);
        }
    }

    private String buildHtmlBody(ContactEmail email) {
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

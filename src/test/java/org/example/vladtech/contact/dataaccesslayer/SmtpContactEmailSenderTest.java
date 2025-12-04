package org.example.vladtech.contact.dataaccesslayer;

import jakarta.mail.BodyPart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.example.vladtech.contact.domain.ContactEmail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpContactEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpContactEmailSender smtpContactEmailSender;

    @Test
    void send_buildsMimeMessageWithExpectedFields() throws Exception {
        // Arrange: use a real MimeMessage instance, created by the mocked JavaMailSender
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ContactEmail email = new ContactEmail(
                "cunninghamadmin4339@gmail.com",         // destinary (To)
                "Hello",                      // title
                "CONTACT_US",                 // template name
                "New contact request from Jane",
                "Kitchen renovation details",
                "Reply to: client@example.com",
                "Jane",                       // senderName
                "client@example.com",         // clientEmail
                LocalDateTime.now()
        );

        // Act
        smtpContactEmailSender.send(email);

        // Assert: From should be the client email (current backend logic)
        InternetAddress from = (InternetAddress) mimeMessage.getFrom()[0];
        assertEquals("client@example.com", from.getAddress());

        // Assert: Reply-To should also be the client email
        InternetAddress[] replyToArr = (InternetAddress[]) mimeMessage.getReplyTo();
        assertNotNull(replyToArr, "Reply-To array should not be null");
        assertTrue(replyToArr.length > 0, "Reply-To array should have at least one element");
        InternetAddress replyTo = replyToArr[0];
        assertEquals("client@example.com", replyTo.getAddress());

        // Assert: To is the admin address
        InternetAddress to =
                (InternetAddress) mimeMessage.getRecipients(MimeMessage.RecipientType.TO)[0];
        assertEquals("cunninghamadmin4339@gmail.com", to.getAddress());

        // Assert: subject
        assertEquals("Hello", mimeMessage.getSubject());

        // Assert: body contains header, body, and footer text
        Object content = mimeMessage.getContent();
        String html;

        if (content instanceof String) {
            html = (String) content;
        } else if (content instanceof MimeMultipart multipart) {
            BodyPart part = multipart.getBodyPart(0);
            Object partContent = part.getContent();
            assertTrue(partContent instanceof String,
                    "Expected first body part to be String, but was " + partContent.getClass());
            html = (String) partContent;
        } else {
            fail("Unexpected content type: " + content.getClass());
            return;
        }

        assertTrue(html.contains("New contact request from Jane"));
        assertTrue(html.contains("Kitchen renovation details"));
        assertTrue(html.contains("Reply to: client@example.com"));
    }
}

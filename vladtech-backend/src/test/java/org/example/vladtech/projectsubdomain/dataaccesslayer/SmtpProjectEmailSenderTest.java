package org.example.vladtech.projectsubdomain.dataaccesslayer;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpProjectEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpProjectEmailSender emailSender;

    private ProjectNotificationEmail notificationEmail;

    private static String readHtml(MimeMessage msg) throws Exception {
        Object content = msg.getContent();

        if (content instanceof String s) {
            return s;
        }

        if (content instanceof jakarta.mail.Multipart mp) {
            for (int i = 0; i < mp.getCount(); i++) {
                var part = mp.getBodyPart(i);
                Object partContent = part.getContent();
                if (partContent instanceof String s) {
                    return s;
                }
            }
        }

        return String.valueOf(content);
    }

    @BeforeEach
    void setUp() {
        emailSender = new SmtpProjectEmailSender(mailSender, "noreply@vladtech.com");

        notificationEmail = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Test Description",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main St, Montreal, Quebec H1A1A1, Canada",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );
    }

    @Test
    void send_ShouldSendEmailSuccessfully_andIncludeOptionalSections() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(notificationEmail);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        String html = (String) mimeMessage.getContent();

        // base content
        assertTrue(html.contains("Project Created"));
        assertTrue(html.contains("Project ID:"));
        assertTrue(html.contains("PROJ-1"));
        assertTrue(html.contains("Project Name:"));
        assertTrue(html.contains("Test Project"));

        // optional sections present
        assertTrue(html.contains("<strong>Description:</strong>"));
        assertTrue(html.contains("<strong>Type:</strong>"));
        assertTrue(html.contains("<strong>Start Date:</strong>"));
        assertTrue(html.contains("<strong>Due Date:</strong>"));
        assertTrue(html.contains("<strong>Location:</strong>"));
    }

    @Test
    void send_ShouldOmitOptionalSections_WhenNullOrBlank() throws Exception {
        ProjectNotificationEmail minimal = new ProjectNotificationEmail(
                "client@example.com",
                "Project Updated: Minimal",
                "PROJ-2",
                "Minimal",
                "Client",
                "   ", // blank description
                null,  // null start date
                null,  // null due date
                "   ", // blank address
                null,  // null type
                "Updated",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(minimal);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        String html = (String) mimeMessage.getContent();

        // base exists
        assertTrue(html.contains("Project Updated"));

        // optional omitted
        assertFalse(html.contains("<strong>Description:</strong>"));
        assertFalse(html.contains("<strong>Type:</strong>"));
        assertFalse(html.contains("<strong>Start Date:</strong>"));
        assertFalse(html.contains("<strong>Due Date:</strong>"));
        assertFalse(html.contains("<strong>Location:</strong>"));
    }

    @Test
    void send_ShouldEscapeHtmlCharacters() throws Exception {
        ProjectNotificationEmail emailWithHtml = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: <Test>",
                "PROJ-1",
                "<script>alert('x')</script>",
                "John & Doe",
                "<b>Bold</b> & <i>Italic</i>",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main & <Ave>",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithHtml);

        String html = readHtml(mimeMessage);

        assertTrue(html.contains("&lt;script&gt;alert('x')&lt;/script&gt;"));
        assertTrue(html.contains("&lt;b&gt;Bold&lt;/b&gt; &amp; &lt;i&gt;Italic&lt;/i&gt;"));
        assertTrue(html.contains("123 Main &amp; &lt;Ave&gt;"));
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<b>"));
        assertFalse(html.contains("<i>"));
        assertFalse(html.contains("&lt;script&gt;alert(&apos;x&apos;)&lt;/script&gt;")); // optional: proves quotes aren't escaped

    }


    @Test
    void send_ShouldWrapMessagingExceptionInRuntimeException() {
        // Force MessagingException inside the try-block by throwing from MimeMessage.setFrom(...)
        MimeMessage throwing = new ThrowingMimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(throwing);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> emailSender.send(notificationEmail));
        assertTrue(ex.getMessage().contains("Failed to send project notification email"));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof MessagingException);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    static class ThrowingMimeMessage extends MimeMessage {
        ThrowingMimeMessage(Session session) {
            super(session);
        }

        @Override
        public void setFrom(jakarta.mail.Address address) throws MessagingException {
            throw new MessagingException("boom");
        }
    }

}

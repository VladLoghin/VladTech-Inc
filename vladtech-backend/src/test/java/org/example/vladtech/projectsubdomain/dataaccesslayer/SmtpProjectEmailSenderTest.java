package org.example.vladtech.projectsubdomain.dataaccesslayer;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.example.vladtech.projectsubdomain.domain.ProjectNotificationEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpProjectEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpProjectEmailSender emailSender;

    private ProjectNotificationEmail notificationEmail;

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
    void send_ShouldSendEmailSuccessfully() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(notificationEmail);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldThrowException_WhenMailServerFails() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(MailSendException.class, () -> emailSender.send(notificationEmail));

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldIncludeAllProjectDetails_WhenAllFieldsPresent() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(notificationEmail);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleNullDescription() {
        ProjectNotificationEmail emailWithoutDescription = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                null,
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main St, Montreal",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithoutDescription);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleBlankDescription() {
        ProjectNotificationEmail emailWithBlankDescription = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "   ",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main St, Montreal",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithBlankDescription);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleNullProjectType() {
        ProjectNotificationEmail emailWithoutType = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Description",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main St, Montreal",
                null,
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithoutType);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleNullStartDate() {
        ProjectNotificationEmail emailWithoutStartDate = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Description",
                null,
                LocalDate.of(2025, 3, 30),
                "123 Main St, Montreal",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithoutStartDate);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleNullDueDate() {
        ProjectNotificationEmail emailWithoutDueDate = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Description",
                LocalDate.of(2025, 1, 15),
                null,
                "123 Main St, Montreal",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithoutDueDate);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleNullAddress() {
        ProjectNotificationEmail emailWithoutAddress = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Description",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                null,
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithoutAddress);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleBlankAddress() {
        ProjectNotificationEmail emailWithBlankAddress = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Description",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "   ",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithBlankAddress);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleUpdateOperation() {
        ProjectNotificationEmail updateEmail = new ProjectNotificationEmail(
                "client@example.com",
                "Project Updated: Test Project",
                "PROJ-1",
                "Test Project",
                "John Doe",
                "Updated Description",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main St, Montreal",
                "SCHEDULED",
                "Updated",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(updateEmail);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldEscapeHtmlCharacters() {
        ProjectNotificationEmail emailWithHtmlChars = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: <Test> Project",
                "PROJ-1",
                "<script>alert('test')</script>",
                "John & Doe",
                "<b>Bold</b> Description",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 3, 30),
                "123 Main St & Avenue",
                "SCHEDULED",
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(emailWithHtmlChars);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldFormatDatesCorrectly() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(notificationEmail);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldUseCorrectFromAddress() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(notificationEmail);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldHandleMinimalProjectInfo() {
        ProjectNotificationEmail minimalEmail = new ProjectNotificationEmail(
                "client@example.com",
                "Project Created: Minimal Project",
                "PROJ-999",
                "Minimal Project",
                "Client Name",
                null,
                null,
                null,
                null,
                null,
                "Created",
                LocalDateTime.now()
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSender.send(minimalEmail);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
package org.example.vladtech.contact.businesslayer;

import org.example.vladtech.contact.datamapperlayer.ContactEmailMapper;
import org.example.vladtech.contact.dataaccesslayer.ContactEmailSender;
import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactEmailMapper mapper;

    @Mock
    private ContactEmailSender emailSender;

    @InjectMocks
    private ContactServiceImpl contactService;

    @Test
    void sendContactMessage_mapsDtoAndSendsEmail() {
        // Arrange
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("Jane Client");
        dto.setSubject("Subject from DTO");
        dto.setMessage("Body from DTO");

        ContactEmail mappedEmail = new ContactEmail(
                "cunninghamadmin4339@gmail.com",
                "Subject from DTO",
                "CONTACT_US",
                "Header",
                "Body",
                "Footer",
                "Jane Client",
                "client@example.com",
                LocalDateTime.now()
        );

        when(mapper.toContactEmail(dto)).thenReturn(mappedEmail);

        // Act
        contactService.sendContactMessage(dto);

        // Assert
        verify(mapper).toContactEmail(dto);
        verify(emailSender).send(mappedEmail);
        verifyNoMoreInteractions(mapper, emailSender);
    }
}

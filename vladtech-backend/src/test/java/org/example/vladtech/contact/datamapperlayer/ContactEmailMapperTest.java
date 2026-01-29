package org.example.vladtech.contact.datamapperlayer;

import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContactEmailMapperTest {

    private static final String TEST_ADMIN_EMAIL = "cunninghamadmin4339@gmail.com";
    private final ContactEmailMapper mapper = new ContactEmailMapper(TEST_ADMIN_EMAIL);

    @Test
    void toContactEmail_mapsAllFieldsCorrectly() {
        // arrange
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("Cunningham");
        dto.setSubject("Kitchen remodel");
        dto.setMessage("I want to remodel my kitchen");

        // act
        ContactEmail email = mapper.toContactEmail(dto, dto.getName(), dto.getEmail());

        // assert
        assertEquals("cunninghamadmin4339@gmail.com", email.getDestinary());
        assertEquals("Kitchen remodel", email.getTitle());
        assertEquals("CONTACT_US", email.getTemplateName());
        assertEquals("New contact request from Cunningham", email.getHeader());
        assertEquals("I want to remodel my kitchen", email.getBody());
        assertEquals("Reply to: Cunningham", email.getFooter()); // Updated to match the method logic
        assertEquals("Cunningham", email.getSenderName());
        assertEquals("client@example.com", email.getClientEmail());
        assertEquals("Cunningham", email.getName());
        assertNotNull(email.getSentDate());
    }

    @Test
    void toHtml_containsKeyPiecesOfInformation() {
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("Cunningham");
        dto.setSubject("Kitchen remodel");
        dto.setMessage("Details about the project");

        ContactEmail email = mapper.toContactEmail(dto, dto.getName(), dto.getEmail());

        String html = mapper.toHtml(email);

        assertTrue(html.contains("New contact request from Cunningham"));
        assertTrue(html.contains("Details about the project"));
        assertTrue(html.contains("Reply to: Cunningham")); // Updated to match the method logic
        assertTrue(html.contains("Sent by Cunningham"));
    }
}

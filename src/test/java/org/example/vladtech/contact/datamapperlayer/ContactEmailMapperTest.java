package org.example.vladtech.contact.datamapperlayer;

import org.example.vladtech.contact.domain.ContactEmail;
import org.example.vladtech.contact.presentationlayer.ContactRequestDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactEmailMapperTest {

    private final ContactEmailMapper mapper = new ContactEmailMapper();

    @Test
    void toContactEmail_mapsAllFieldsCorrectly() {
        // arrange
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("Cunningham");
        dto.setSubject("Kitchen remodel");
        dto.setMessage("I want to remodel my kitchen");

        // act
        ContactEmail email = mapper.toContactEmail(dto);

        // assert
        assertEquals("cunninghamadmin4339@gmail.com", email.getDestinary());
        assertEquals("Kitchen remodel", email.getTitle());
        assertEquals("CONTACT_US", email.getTemplateName());
        assertEquals("New contact request from Cunningham", email.getHeader());
        assertEquals("I want to remodel my kitchen", email.getBody());
        assertEquals("Reply to: client@example.com", email.getFooter());
        assertEquals("Cunningham", email.getSenderName());
        assertEquals("client@example.com", email.getClientEmail());
        assertNotNull(email.getSentDate());
    }

    @Test
    void toHtml_containsKeyPiecesOfInformation() {
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("Cunningham");
        dto.setSubject("Kitchen remodel");
        dto.setMessage("Details about the project");

        ContactEmail email = mapper.toContactEmail(dto);

        String html = mapper.toHtml(email);

        assertTrue(html.contains("New contact request from Cunningham"));
        assertTrue(html.contains("Details about the project"));
        assertTrue(html.contains("Reply to: client@example.com"));
        assertTrue(html.contains("Sent by Cunningham"));
    }
}

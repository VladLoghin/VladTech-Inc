package org.example.vladtech.contact.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.contact.businesslayer.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendContact_returnsOk() throws Exception {
        // Arrange: build a DTO like the real API expects
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("John Doe");
        dto.setSubject("Test Subject");
        dto.setMessage("Hello from test");

        String json = objectMapper.writeValueAsString(dto);

        // Act + Assert
        mockMvc.perform(
                        post("/api/contact")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());

        verify(contactService).sendContactMessage(any(ContactRequestDto.class));
    }
}

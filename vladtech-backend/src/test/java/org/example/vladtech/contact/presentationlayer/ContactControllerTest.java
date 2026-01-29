package org.example.vladtech.contact.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.auth.service.UserManagementService;
import org.example.vladtech.contact.businesslayer.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc // ✅ filters ON (default)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private UserManagementService userManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendContact_returnsOk() throws Exception {
        ContactRequestDto dto = new ContactRequestDto();
        dto.setEmail("client@example.com");
        dto.setName("");
        dto.setSubject("Test Subject");
        dto.setMessage("Hello from test");

        String json = objectMapper.writeValueAsString(dto);

        // Controller calls: userManagementService.getUserNameById(userId)
        when(userManagementService.getUserNameById("auth0|test-user-123"))
                .thenReturn(""); // force fallback to email prefix

        mockMvc.perform(
                post("/api/contact")
                        .with(jwt().jwt(j -> {
                            j.subject("auth0|test-user-123");
                            j.claim("email", "client@example.com");
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());

        verify(contactService).sendContactMessage(any(ContactRequestDto.class), eq("client"), eq("client@example.com"));
    }
}

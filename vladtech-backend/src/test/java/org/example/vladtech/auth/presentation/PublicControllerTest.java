package org.example.vladtech.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vladtech.auth.service.UserManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserManagementServiceImpl userManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    // --------------------
    // /sync-profile
    // --------------------

    @Test
    void syncProfile_withJwt_callsService_andReturnsOk() throws Exception {
        String userId = "auth0|123";
        String email = "user@test.com";
        String name = "Test User";

        mockMvc.perform(post("/api/public/sync-profile")
                        .with(jwt().jwt(j -> {
                            j.subject(userId);
                            j.claim("email", email);
                            j.claim("name", name);
                        })))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile synced"));

        verify(userManagementService).syncUserProfile(userId, email, name);
    }

    @Test
    void syncProfile_withMissingName_fallsBackToEmailPrefix() throws Exception {
        String userId = "auth0|123";
        String email = "fallback@test.com";

        mockMvc.perform(post("/api/public/sync-profile")
                        .with(jwt().jwt(j -> {
                            j.subject(userId);
                            j.claim("email", email);
                        })))
                .andExpect(status().isOk());

        verify(userManagementService)
                .syncUserProfile(userId, email, "fallback");
    }

    // --------------------
    // /profile
    // --------------------

    @Test
    void getProfile_whenNicknameExists_returnsNickname() throws Exception {
        String userId = "auth0|123";
        when(userManagementService.getUserNameById(userId))
                .thenReturn("Saved Nickname");

        mockMvc.perform(get("/api/public/profile")
                        .with(jwt().jwt(j -> j.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(content().string("User Name: Saved Nickname"));
    }

    @Test
    void getProfile_whenNicknameMissing_fallsBackToEmailPrefix() throws Exception {
        String userId = "auth0|123";
        String email = "fallback@test.com";

        when(userManagementService.getUserNameById(userId)).thenReturn(null);

        mockMvc.perform(get("/api/public/profile")
                        .with(jwt().jwt(j -> {
                            j.subject(userId);
                            j.claim("email", email);
                        })))
                .andExpect(status().isOk())
                .andExpect(content().string("User Name: fallback"));
    }

    // --------------------
    // /profile/update-name
    // --------------------

    @Test
    void updateUserName_validName_returnsOk() throws Exception {
        String userId = "auth0|123";

        when(userManagementService.updateUserName(userId, "NewName"))
                .thenReturn("User name updated successfully.");

        mockMvc.perform(patch("/api/public/profile/update-name")
                        .with(jwt().jwt(j -> j.subject(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "NewName")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string("User name updated successfully."));

        verify(userManagementService).updateUserName(userId, "NewName");
    }

    @Test
    void updateUserName_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/public/profile/update-name")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid name."));

        verifyNoInteractions(userManagementService);
    }

    

    @Test
    void updateUserName_genericException_returns500() throws Exception {
        String userId = "auth0|123";

        when(userManagementService.updateUserName(eq(userId), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(patch("/api/public/profile/update-name")
                        .with(jwt().jwt(j -> j.subject(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewName\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error: boom"));
    }
}

package org.example.vladtech.estimates.presentation;

import org.example.vladtech.estimates.business.EstimateSettingsService;
import org.example.vladtech.estimates.data.EstimateSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstimateSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstimateSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstimateSettingsService estimateSettingsService;

    @Test
    void getSettings_returnsOkAndBody() throws Exception {
        EstimateSettings settings = EstimateSettings.defaultSettings();
        when(estimateSettingsService.getSettings()).thenReturn(settings);

        mockMvc.perform(get("/api/estimates/config")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laborRate").value(settings.getLaborRate().doubleValue()));
    }

    @Test
    @WithMockUser(authorities = "Admin")
    void updateSettings_withAdmin_returnsOk() throws Exception {
        EstimateSettings settings = EstimateSettings.defaultSettings();
        settings.setLaborRate(new BigDecimal("55.00"));
        when(estimateSettingsService.updateSettings(org.mockito.ArgumentMatchers.any(EstimateSettings.class))).thenReturn(settings);

        String json = "{\"laborRate\":55.00}";

        mockMvc.perform(put("/api/estimates/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laborRate").value(55.0));
    }
}

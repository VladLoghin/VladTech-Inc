package org.example.vladtech.estimates.presentation;

import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RenovationEstimateController.class)
@AutoConfigureMockMvc(addFilters = false)
class RenovationEstimateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstimationService estimationService;

    @MockitoBean
    private RenovationEstimateResponseMapper responseMapper;

    @Test
    void calculateEstimate_ShouldReturnResponse() throws Exception {
        RenovationProject calculatedProject = new RenovationProject();
        RenovationEstimateResponseModel responseModel = new RenovationEstimateResponseModel();
        responseModel.setTotalPrice(BigDecimal.valueOf(12075.00));

        when(estimationService.calculateEstimate(any())).thenReturn(calculatedProject);
        when(responseMapper.toResponse(calculatedProject)).thenReturn(responseModel);

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("squareFeet", "100")
                        .param("materialCostPerSqFt", "20")
                        .param("locationFactor", "1.2")
                        .param("taxRate", "0.15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(12075.00));
    }

    @Test
    void calculateEstimate_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        mockMvc.perform(get("/api/estimates/calculate")
                        .param("squareFeet", "-100")
                        .param("materialCostPerSqFt", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }
}

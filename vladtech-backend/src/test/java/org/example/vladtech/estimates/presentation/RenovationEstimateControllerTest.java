package org.example.vladtech.estimates.presentation;

import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.exceptions.EstimationException;
import org.example.vladtech.estimates.exceptions.EstimatesExceptionHandler;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RenovationEstimateController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(EstimatesExceptionHandler.class)
@WithMockUser
class RenovationEstimateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstimationService estimationService;

    @MockitoBean
    private RenovationEstimateResponseMapper responseMapper;

    @Test
    void returnsEstimateForRoofingRequest() throws Exception {
        RoofingReplace calculated = new RoofingReplace();
        calculated.setSquareFeet(new BigDecimal("100"));
        calculated.setEstimatePrice(new BigDecimal("10000"));
        calculated.setTaxAmount(new BigDecimal("1500"));
        calculated.setTotalPrice(new BigDecimal("11500"));

        when(estimationService.calculateEstimate(any())).thenReturn(calculated);
        when(responseMapper.toResponse(calculated)).thenReturn(
                new RenovationEstimateResponseModel(
                        calculated.getSquareFeet(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        calculated.getEstimatePrice(),
                        calculated.getTaxAmount(),
                        calculated.getTotalPrice())
        );

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "ROOFING_REPLACE")
                        .param("squareFeet", "100")
                        .param("materialCostPerSqFt", "8")
                        .param("roofPitch", "1.2")
                        .param("roofMaterial", RoofMaterial.METAL.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatePrice").value(10000))
                .andExpect(jsonPath("$.taxAmount").value(1500))
                .andExpect(jsonPath("$.totalPrice").value(11500));
    }

    @Test
    void returnsBadRequestForInvalidSquareFeet() throws Exception {
        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "ROOFING_REPLACE")
                        .param("squareFeet", "0")
                        .param("materialCostPerSqFt", "8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(containsString("Square feet must be positive")));
    }

    @Test
    void mapsEstimationExceptionToErrorResponse() throws Exception {
        when(estimationService.calculateEstimate(any())).thenThrow(new EstimationException("E200", "calc failed"));

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "ROOFING_REPLACE")
                        .param("squareFeet", "100")
                        .param("materialCostPerSqFt", "8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("E200"))
                .andExpect(jsonPath("$.message").value("calc failed"));
    }

    @Test
    void usesAreaSqFtWhenSquareFeetMissing() throws Exception {
        RoofingReplace calculated = new RoofingReplace();
        calculated.setSquareFeet(new BigDecimal("80"));
        calculated.setEstimatePrice(new BigDecimal("8000"));
        calculated.setTaxAmount(new BigDecimal("1200"));
        calculated.setTotalPrice(new BigDecimal("9200"));

        when(estimationService.calculateEstimate(any())).thenReturn(calculated);
        when(responseMapper.toResponse(calculated)).thenReturn(
                new RenovationEstimateResponseModel(
                        calculated.getSquareFeet(), null, null, null, null, null, null,
                        calculated.getEstimatePrice(), calculated.getTaxAmount(), calculated.getTotalPrice())
        );

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "ROOFING_REPLACE")
                        .param("areaSqFt", "80")
                        .param("materialCostPerSqFt", "7"))
                .andExpect(status().isOk());
    }

    @Test
    void returnsBadRequestWhenBothSqFtMissing() throws Exception {
        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "ROOFING_REPLACE")
                        .param("materialCostPerSqFt", "8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test
    void returnsBadRequestForNegativeMaterialCost() throws Exception {
        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "SIDING_REPLACE")
                        .param("squareFeet", "100")
                        .param("materialCostPerSqFt", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(containsString("non-negative")));
    }

    @Test
    void returnsBadRequestForNegativeLocationFactor() throws Exception {
        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "ROOFING_REPLACE")
                        .param("squareFeet", "100")
                        .param("materialCostPerSqFt", "8")
                        .param("locationFactor", "-0.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(containsString("Location factor")));
    }

    @Test
    void sidingReplacePathWithNullStories() throws Exception {
        when(estimationService.calculateEstimate(any())).thenReturn(new RoofingReplace());
        when(responseMapper.toResponse(any())).thenReturn(
                new RenovationEstimateResponseModel(BigDecimal.TEN, null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN)
        );

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "SIDING_REPLACE")
                        .param("squareFeet", "100")
                        .param("materialCostPerSqFt", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void kitchenRemodelWithAllEnums() throws Exception {
        when(estimationService.calculateEstimate(any())).thenReturn(new RoofingReplace());
        when(responseMapper.toResponse(any())).thenReturn(
                new RenovationEstimateResponseModel(BigDecimal.TEN, null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN)
        );

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "KITCHEN_REMODEL")
                        .param("squareFeet", "150")
                        .param("materialCostPerSqFt", "80")
                        .param("flooringMaterial", "HARDWOOD")
                        .param("cabinetQuality", "CUSTOM")
                        .param("countertopMaterial", "GRANITE"))
                .andExpect(status().isOk());
    }

    @Test
    void genericProjectPath() throws Exception {
        when(estimationService.calculateEstimate(any())).thenReturn(new RoofingReplace());
        when(responseMapper.toResponse(any())).thenReturn(
                new RenovationEstimateResponseModel(BigDecimal.TEN, null, null, null, null, null, null, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN)
        );

        mockMvc.perform(get("/api/estimates/calculate")
                        .param("projectType", "UNKNOWN_TYPE")
                        .param("squareFeet", "50")
                        .param("materialCostPerSqFt", "15"))
                .andExpect(status().isOk());
    }
}
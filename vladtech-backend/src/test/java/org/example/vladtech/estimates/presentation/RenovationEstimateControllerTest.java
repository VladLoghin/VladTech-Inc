package org.example.vladtech.estimates.presentation;

import org.example.vladtech.estimates.business.EstimationService;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.mapperlayer.RenovationEstimateResponseMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RenovationEstimateControllerTest {

    private final EstimationService estimationService = mock(EstimationService.class);
    private final RenovationEstimateResponseMapper responseMapper = mock(RenovationEstimateResponseMapper.class);
    private final RenovationEstimateController controller = new RenovationEstimateController(estimationService, responseMapper);

    @Test
    void calculateEstimate_ShouldReturnResponse() {
        RenovationProject project = new RenovationProject();
        RenovationProject calculatedProject = new RenovationProject();
        RenovationEstimateResponseModel responseModel = new RenovationEstimateResponseModel();

        when(estimationService.calculateEstimate(any())).thenReturn(calculatedProject);
        when(responseMapper.toResponse(calculatedProject)).thenReturn(responseModel);

        ResponseEntity<RenovationEstimateResponseModel> response = controller.calculateEstimate(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(0.15)
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseModel, response.getBody());
    }
}
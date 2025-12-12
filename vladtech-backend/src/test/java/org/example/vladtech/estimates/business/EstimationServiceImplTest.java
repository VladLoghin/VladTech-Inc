package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.RenovationProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EstimationServiceImplTest {

    private EstimationServiceImpl estimationService;

    @BeforeEach
    void setUp() {
        estimationService = new EstimationServiceImpl();
        ReflectionTestUtils.setField(estimationService, "laborRate", BigDecimal.valueOf(50.00));
        ReflectionTestUtils.setField(estimationService, "overheadRate", BigDecimal.valueOf(0.15));
        ReflectionTestUtils.setField(estimationService, "contingencyRate", BigDecimal.valueOf(0.10));
        ReflectionTestUtils.setField(estimationService, "taxRate", BigDecimal.valueOf(0.15));
    }

    @Test
    void calculateEstimate_ShouldCalculateCorrectValues() {
        RenovationProject project = new RenovationProject();
        project.setSquareFeet(BigDecimal.valueOf(100));
        project.setMaterialCostPerSqFt(BigDecimal.valueOf(20));
        project.setLocationFactor(BigDecimal.valueOf(1.2));

        RenovationProject result = estimationService.calculateEstimate(project);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50.00), result.getLaborRate());
        assertEquals(BigDecimal.valueOf(0.15), result.getOverheadRate());
        assertEquals(BigDecimal.valueOf(0.10), result.getContingencyRate());
        assertEquals(BigDecimal.valueOf(0.15), result.getTaxRate());
        assertEquals(BigDecimal.valueOf(10500.00).setScale(2), result.getEstimatePrice());
        assertEquals(BigDecimal.valueOf(1575.00).setScale(2), result.getTaxAmount());
        assertEquals(BigDecimal.valueOf(12075.00).setScale(2), result.getTotalPrice());
    }

    @Test
    void calculateEstimate_ShouldHandleNullValuesGracefully() {
        RenovationProject project = new RenovationProject();
        project.setSquareFeet(null);
        project.setMaterialCostPerSqFt(null);
        project.setLocationFactor(null);

        RenovationProject result = estimationService.calculateEstimate(project);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2), result.getEstimatePrice());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTaxAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalPrice());
    }

    @Test
    void calculateEstimate_ShouldThrowException_WhenProjectIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            estimationService.calculateEstimate(null);
        });

        assertEquals("project cannot be null", exception.getMessage());
    }
}
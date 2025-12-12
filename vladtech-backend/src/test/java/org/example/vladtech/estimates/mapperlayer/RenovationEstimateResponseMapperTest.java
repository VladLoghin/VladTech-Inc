package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.presentation.RenovationEstimateResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RenovationEstimateResponseMapperTest {

    private final RenovationEstimateResponseMapper mapper = Mappers.getMapper(RenovationEstimateResponseMapper.class);

    @Test
    void toResponse_ShouldMapFieldsCorrectly() {
        RenovationProject project = new RenovationProject(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(0.15),
                BigDecimal.valueOf(0.10),
                BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(0.15),
                BigDecimal.valueOf(10500),
                BigDecimal.valueOf(1575),
                BigDecimal.valueOf(12075)
        );

        RenovationEstimateResponseModel response = mapper.toResponse(project);

        assertNotNull(response);
        assertEquals(project.getSquareFeet(), response.getSquareFeet());
        assertEquals(project.getLaborRate(), response.getLaborRate());
        assertEquals(project.getMaterialCostPerSqFt(), response.getMaterialCostPerSqFt());
        assertEquals(project.getTotalPrice(), response.getTotalPrice());
    }
}
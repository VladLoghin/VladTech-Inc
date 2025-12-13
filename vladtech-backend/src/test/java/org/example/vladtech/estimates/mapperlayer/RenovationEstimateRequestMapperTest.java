package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.presentation.RenovationEstimateRequestModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RenovationEstimateRequestMapperTest {

    private final RenovationEstimateRequestMapper mapper = Mappers.getMapper(RenovationEstimateRequestMapper.class);

    @Test
    void toEntity_ShouldMapFieldsCorrectly() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(1.2)
        );

        RenovationProject project = mapper.toEntity(request);

        assertNotNull(project);
        assertEquals(request.getSquareFeet(), project.getSquareFeet());
        assertEquals(request.getMaterialCostPerSqFt(), project.getMaterialCostPerSqFt());
        assertEquals(request.getLocationFactor(), project.getLocationFactor());
    }
}
package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.presentation.RenovationEstimateRequestModel;
import org.example.vladtech.estimates.presentation.RenovationEstimateResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RenovationEstimateMapperTest {

    private RenovationEstimateRequestMapper requestMapper;
    private RenovationEstimateResponseMapper responseMapper;

    @BeforeEach
    void setUp() {
        requestMapper = Mappers.getMapper(RenovationEstimateRequestMapper.class);
        responseMapper = Mappers.getMapper(RenovationEstimateResponseMapper.class);
    }

    @Test
    void mapsRequestToRoofingReplace() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel(
                new BigDecimal("100"),
                new BigDecimal("12"),
                new BigDecimal("1.10"),
                2,
                true,
                "VINYL",
                new BigDecimal("1.3"),
                "METAL",
                true,
                true,
                2,
                5000.0,
                true,
                true,
                "HARDWOOD",
                "PREMIUM",
                "QUARTZ"
        );

        RoofingReplace roofing = requestMapper.toRoofingReplace(request);

        assertEquals(new BigDecimal("100"), roofing.getSquareFeet());
        assertEquals(new BigDecimal("12"), roofing.getMaterialCostPerSqFt());
        assertEquals(new BigDecimal("1.10"), roofing.getLocationFactor());
        assertEquals(new BigDecimal("1.3"), roofing.getRoofPitch());
        assertEquals(RoofMaterial.METAL, roofing.getRoofMaterial());
    }

    @Test
    void mapsRequestToSidingReplace() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel(
                new BigDecimal("150"),
                new BigDecimal("9"),
                new BigDecimal("1.00"),
                3,
                false,
                "STONE_VENEER",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        SidingReplace siding = requestMapper.toSidingReplace(request);

        assertEquals(3, siding.getStories());
        assertFalse(siding.isIncludeInsulation());
        assertEquals(SidingMaterial.STONE_VENEER, siding.getSidingMaterial());
    }

    @Test
    void mapsRequestToKitchenRemodel() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel(
                new BigDecimal("200"),
                new BigDecimal("80"),
                new BigDecimal("1.05"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                3000.0,
                true,
                false,
                "TILE",
                "STOCK",
                "GRANITE"
        );

        KitchenRemodel kitchen = requestMapper.toKitchenRemodel(request);

        assertEquals(3000.0, kitchen.getApplianceAllowance());
        assertTrue(kitchen.getPlumbingChanges());
        assertFalse(kitchen.getElectricalChanges());
    }

    @Test
    void mapsEntityToResponse() {
        RenovationProject project = new RenovationProject(
                new BigDecimal("120"),
                new BigDecimal("50"),
                new BigDecimal("10"),
                new BigDecimal("0.15"),
                new BigDecimal("0.10"),
                new BigDecimal("1.00"),
                new BigDecimal("0.15"),
                new BigDecimal("8000"),
                new BigDecimal("1200"),
                new BigDecimal("9200")
        );

        RenovationEstimateResponseModel response = responseMapper.toResponse(project);

        assertEquals(project.getSquareFeet(), response.getSquareFeet());
        assertEquals(project.getEstimatePrice(), response.getEstimatePrice());
        assertEquals(project.getTotalPrice(), response.getTotalPrice());
    }
}
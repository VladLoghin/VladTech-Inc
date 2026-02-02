package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.data.windowanddoor.WindowDoorReplace;
import org.example.vladtech.estimates.data.windowanddoor.WindowType;
import org.example.vladtech.estimates.data.windowanddoor.DoorType;
import org.example.vladtech.estimates.data.patio.DeckPatioAddition;
import org.example.vladtech.estimates.data.patio.DeckMaterial;
import org.example.vladtech.estimates.data.floor.FloorReplace;
import org.example.vladtech.estimates.data.shared.FlooringMaterial;
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
                "QUARTZ",
                null, null, null, null,
                null, null, null, null, null,
                null, null, null
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
                null,
                null, null, null, null,
                null, null, null, null, null,
                null, null, null
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
                "GRANITE",
                null, null, null, null,
                null, null, null, null, null,
                null, null, null
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

    @Test
    void mapsRequestToWindowDoorReplace() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel();
        request.setWindowType("CASEMENT");
        request.setDoorType("FIBERGLASS");
        request.setWindowCount(5);
        request.setDoorCount(2);

        WindowDoorReplace windowDoor = requestMapper.toWindowDoorReplace(request);

        assertEquals(WindowType.CASEMENT, windowDoor.getWindowType());
        assertEquals(DoorType.FIBERGLASS, windowDoor.getDoorType());
        assertEquals(5, windowDoor.getWindowCount());
        assertEquals(2, windowDoor.getDoorCount());
        assertNull(windowDoor.getLaborRate());
        assertNull(windowDoor.getEstimatePrice());
    }

    @Test
    void mapsRequestToDeckPatioAddition() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel();
        request.setDeckMaterial("COMPOSITE");
        request.setHasRailing(true);
        request.setStairsCount(2);
        request.setIsCovered(true);
        request.setAreaSqFt(250.0);

        DeckPatioAddition deckPatio = requestMapper.toDeckPatioAddition(request);

        assertEquals(DeckMaterial.COMPOSITE, deckPatio.getDeckMaterial());
        assertTrue(deckPatio.getHasRailing());
        assertEquals(2, deckPatio.getStairsCount());
        assertTrue(deckPatio.getIsCovered());
        assertEquals(250.0, deckPatio.getAreaSqFt());
        assertNull(deckPatio.getLaborRate());
        assertNull(deckPatio.getEstimatePrice());
    }

    @Test
    void mapsRequestToFloorReplace() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel();
        request.setSquareFeet(new BigDecimal("300"));
        request.setMaterialCostPerSqFt(new BigDecimal("8"));
        request.setLocationFactor(new BigDecimal("1.10"));
        request.setExistingFloorMaterial("CARPET");
        request.setNewFloorMaterial("HARDWOOD");
        request.setSubfloorRepairNeeded(true);

        FloorReplace floorReplace = requestMapper.toFloorReplace(request);

        assertEquals(new BigDecimal("300"), floorReplace.getSquareFeet());
        assertEquals(new BigDecimal("8"), floorReplace.getMaterialCostPerSqFt());
        assertEquals(new BigDecimal("1.10"), floorReplace.getLocationFactor());
        assertEquals(FlooringMaterial.CARPET, floorReplace.getExistingFloorMaterial());
        assertEquals(FlooringMaterial.HARDWOOD, floorReplace.getNewFloorMaterial());
        assertTrue(floorReplace.getSubfloorRepairNeeded());
        assertNull(floorReplace.getLaborRate());
        assertNull(floorReplace.getEstimatePrice());
    }

    @Test
    void mapsRequestToWindowDoorReplaceWithNullValues() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel();
        request.setWindowType(null);
        request.setDoorType(null);
        request.setWindowCount(0);
        request.setDoorCount(0);

        WindowDoorReplace windowDoor = requestMapper.toWindowDoorReplace(request);

        assertNull(windowDoor.getWindowType());
        assertNull(windowDoor.getDoorType());
        assertEquals(0, windowDoor.getWindowCount());
        assertEquals(0, windowDoor.getDoorCount());
    }

    @Test
    void mapsRequestToDeckPatioAdditionWithNullValues() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel();
        request.setDeckMaterial(null);
        request.setHasRailing(null);
        request.setStairsCount(null);
        request.setIsCovered(null);
        request.setAreaSqFt(null);

        DeckPatioAddition deckPatio = requestMapper.toDeckPatioAddition(request);

        assertNull(deckPatio.getDeckMaterial());
        assertNull(deckPatio.getHasRailing());
        assertNull(deckPatio.getStairsCount());
        assertNull(deckPatio.getIsCovered());
        assertNull(deckPatio.getAreaSqFt());
    }

    @Test
    void mapsRequestToFloorReplaceWithNullFlooringMaterials() {
        RenovationEstimateRequestModel request = new RenovationEstimateRequestModel();
        request.setSquareFeet(new BigDecimal("200"));
        request.setMaterialCostPerSqFt(new BigDecimal("5"));
        request.setExistingFloorMaterial(null);
        request.setNewFloorMaterial(null);
        request.setSubfloorRepairNeeded(false);

        FloorReplace floorReplace = requestMapper.toFloorReplace(request);

        assertNull(floorReplace.getExistingFloorMaterial());
        assertNull(floorReplace.getNewFloorMaterial());
        assertFalse(floorReplace.getSubfloorRepairNeeded());
    }
}
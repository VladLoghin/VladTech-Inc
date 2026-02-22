package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.EstimateSettings;
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
import org.example.vladtech.estimates.exceptions.EstimationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstimationServiceImplTest {

    private EstimationServiceImpl service;
    
    @Mock
    private EstimateSettingsService estimateSettingsService;

    @BeforeEach
    void setUp() {
        // Use default settings for all tests
        EstimateSettings defaultSettings = EstimateSettings.defaultSettings();
        lenient().when(estimateSettingsService.getSettings()).thenReturn(defaultSettings);
        
        service = new EstimationServiceImpl(estimateSettingsService);
    }

    @Test
    void throwsWhenProjectNull() {
        EstimationException ex = assertThrows(EstimationException.class, () -> service.calculateEstimate(null));
        assertEquals("E000", ex.getCode());
    }

    @Test
    void sidingEstimateCalculatesTotals() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("100"));
        siding.setMaterialCostPerSqFt(new BigDecimal("10"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(2); // triggers extra labor
        siding.setIncludeInsulation(true); // adds insulation adder
        siding.setSidingMaterial(SidingMaterial.VINYL);

        service.calculateEstimate(siding);

        assertEquals(new BigDecimal("8218.75"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1232.81"), siding.getTaxAmount());
        assertEquals(new BigDecimal("9451.56"), siding.getTotalPrice());
    }

    @Test
    void sidingDefaultsWhenMaterialMissing() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("80"));
        siding.setMaterialCostPerSqFt(new BigDecimal("7"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(1);
        siding.setIncludeInsulation(false);
        siding.setSidingMaterial(null); // defaults to factor 1.0

        service.calculateEstimate(siding);

        assertEquals(new BigDecimal("5700.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("855.00"), siding.getTaxAmount());
        assertEquals(new BigDecimal("6555.00"), siding.getTotalPrice());
    }

    @Test
    void roofingEstimateAppliesPitchTearOffAndSkylights() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("100"));
        roof.setAreaSqFt(new BigDecimal("100"));
        roof.setMaterialCostPerSqFt(new BigDecimal("12"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(3); // extra labor for 2 additional stories
        roof.setRoofPitch(new BigDecimal("1.5"));
        roof.setRoofMaterial(RoofMaterial.METAL); // factor 1.2
        roof.setTearOffRequired(true);
        roof.setHasSkylights(true);
        roof.setNumSkylights(2);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("14159.38"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("2123.91"), roof.getTaxAmount());
        assertEquals(new BigDecimal("16283.28"), roof.getTotalPrice());
    }

    @Test
    void roofingHandlesNullMaterialNoSkylightsOrTearOff() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("120"));
        roof.setAreaSqFt(new BigDecimal("120"));
        roof.setMaterialCostPerSqFt(new BigDecimal("9"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(1);
        roof.setRoofPitch(new BigDecimal("1.0"));
        roof.setRoofMaterial(null);
        roof.setHasSkylights(false);
        roof.setTearOffRequired(false);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("9735.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1460.25"), roof.getTaxAmount());
        assertEquals(new BigDecimal("11195.25"), roof.getTotalPrice());
    }

    @Test
    void kitchenEstimateUsesMaterialCostPerSqFt() {
        KitchenRemodel kitchen = new KitchenRemodel();
        kitchen.setSquareFeet(new BigDecimal("200"));
        kitchen.setMaterialCostPerSqFt(new BigDecimal("76"));
        kitchen.setLocationFactor(BigDecimal.ONE);
        kitchen.setApplianceAllowance(0.0);
        kitchen.setPlumbingChanges(false);
        kitchen.setElectricalChanges(false);

        service.calculateEstimate(kitchen);

        // material cost = 200 * 76 = 15200; overhead 2280; contingency 1520; estimate 19000; tax 2850; total 21850
        assertEquals(new BigDecimal("19000.00"), kitchen.getEstimatePrice());
        assertEquals(new BigDecimal("2850.00"), kitchen.getTaxAmount());
        assertEquals(new BigDecimal("21850.00"), kitchen.getTotalPrice());
    }

    @Test
    void genericProjectUsesDefaultLocationFactor() {
        RenovationProject project = new RenovationProject();
        project.setSquareFeet(new BigDecimal("50"));
        project.setMaterialCostPerSqFt(new BigDecimal("20"));
        project.setLocationFactor(null); // default to 1

        service.calculateEstimate(project);

        assertEquals(new BigDecimal("4375.00"), project.getEstimatePrice());
        assertEquals(new BigDecimal("656.25"), project.getTaxAmount());
        assertEquals(new BigDecimal("5031.25"), project.getTotalPrice());
    }

    @Test
    void roofingDefaultsWhenMaterialAndLocationMissing() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("100"));
        roof.setRoofPitch(new BigDecimal("1.0")); // pitch factor = 1.1
        roof.setMaterialCostPerSqFt(new BigDecimal("8"));
        roof.setLocationFactor(null); // defaults to 1
        roof.setStories(1);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("7975.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1196.25"), roof.getTaxAmount());
        assertEquals(new BigDecimal("9171.25"), roof.getTotalPrice());
    }

    @Test
    void sidingStoneVeneerFactorApplied() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("150"));
        siding.setMaterialCostPerSqFt(new BigDecimal("12"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(1);
        siding.setIncludeInsulation(false);
        siding.setSidingMaterial(SidingMaterial.STONE_VENEER); // factor 1.45

        service.calculateEstimate(siding);

        assertEquals(new BigDecimal("11625.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1743.75"), siding.getTaxAmount());
        assertEquals(new BigDecimal("13368.75"), siding.getTotalPrice());
    }

    @Test
    void kitchenEstimateAddsPlumbingAndElectrical() {
        KitchenRemodel kitchen = new KitchenRemodel();
        kitchen.setSquareFeet(new BigDecimal("120"));
        kitchen.setMaterialCostPerSqFt(new BigDecimal("80"));
        kitchen.setLocationFactor(BigDecimal.ONE);
        kitchen.setApplianceAllowance(5000.0);
        kitchen.setPlumbingChanges(true);
        kitchen.setElectricalChanges(true);

        service.calculateEstimate(kitchen);

        // base 18100; overhead 2715; contingency 1810; estimate 22625; tax 3393.75; total 26018.75
        assertEquals(new BigDecimal("22625.00"), kitchen.getEstimatePrice());
        assertEquals(new BigDecimal("3393.75"), kitchen.getTaxAmount());
        assertEquals(new BigDecimal("26018.75"), kitchen.getTotalPrice());
    }

    @Test
    void rejectsZeroSquareFeet() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(BigDecimal.ZERO);
        siding.setMaterialCostPerSqFt(new BigDecimal("5"));
        EstimationException ex = assertThrows(EstimationException.class, () -> service.calculateEstimate(siding));
        assertEquals("E101", ex.getCode());
    }

    @Test
    void rejectsNegativeMaterialCost() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("10"));
        siding.setMaterialCostPerSqFt(new BigDecimal("-1"));
        EstimationException ex = assertThrows(EstimationException.class, () -> service.calculateEstimate(siding));
        assertEquals("E102", ex.getCode());
    }

    @Test
    void sidingVinylFactorApplied() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("100"));
        siding.setMaterialCostPerSqFt(new BigDecimal("8"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(1);
        siding.setIncludeInsulation(false);
        siding.setSidingMaterial(SidingMaterial.VINYL);

        service.calculateEstimate(siding);

        assertEquals(new BigDecimal("7250.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1087.50"), siding.getTaxAmount());
        assertEquals(new BigDecimal("8337.50"), siding.getTotalPrice());
    }

    @Test
    void sidingWoodFactorApplied() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("100"));
        siding.setMaterialCostPerSqFt(new BigDecimal("10"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(1);
        siding.setIncludeInsulation(false);
        siding.setSidingMaterial(SidingMaterial.WOOD);

        service.calculateEstimate(siding);

            assertEquals(new BigDecimal("7500.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1125.00"), siding.getTaxAmount());
        assertEquals(new BigDecimal("8625.00"), siding.getTotalPrice());
    }

    @Test
    void sidingFiberCementFactorApplied() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("100"));
        siding.setMaterialCostPerSqFt(new BigDecimal("10"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(1);
        siding.setIncludeInsulation(false);
        siding.setSidingMaterial(SidingMaterial.FIBER_CEMENT);

        service.calculateEstimate(siding);

        assertEquals(new BigDecimal("7500.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1125.00"), siding.getTaxAmount());
        assertEquals(new BigDecimal("8625.00"), siding.getTotalPrice());
    }

    @Test
    void sidingBrickFactorApplied() {
        SidingReplace siding = new SidingReplace();
        siding.setSquareFeet(new BigDecimal("100"));
        siding.setMaterialCostPerSqFt(new BigDecimal("10"));
        siding.setLocationFactor(BigDecimal.ONE);
        siding.setStories(1);
        siding.setIncludeInsulation(false);
        siding.setSidingMaterial(SidingMaterial.BRICK);

        service.calculateEstimate(siding);

        assertEquals(new BigDecimal("7500.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1125.00"), siding.getTaxAmount());
        assertEquals(new BigDecimal("8625.00"), siding.getTotalPrice());
    }

    @Test
    void roofingAsphaltFactorApplied() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("100"));
        roof.setAreaSqFt(new BigDecimal("100"));
        roof.setMaterialCostPerSqFt(new BigDecimal("8"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(1);
        roof.setRoofPitch(BigDecimal.ONE);
        roof.setRoofMaterial(RoofMaterial.ASPHALT);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("7975.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1196.25"), roof.getTaxAmount());
        assertEquals(new BigDecimal("9171.25"), roof.getTotalPrice());
    }

    @Test
    void roofingClayFactorApplied() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("100"));
        roof.setAreaSqFt(new BigDecimal("100"));
        roof.setMaterialCostPerSqFt(new BigDecimal("10"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(1);
        roof.setRoofPitch(BigDecimal.ONE);
        roof.setRoofMaterial(RoofMaterial.CLAY);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("8250.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1237.50"), roof.getTaxAmount());
        assertEquals(new BigDecimal("9487.50"), roof.getTotalPrice());
    }

    @Test
    void roofingSlateFactorApplied() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("100"));
        roof.setAreaSqFt(new BigDecimal("100"));
        roof.setMaterialCostPerSqFt(new BigDecimal("10"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(1);
        roof.setRoofPitch(BigDecimal.ONE);
        roof.setRoofMaterial(RoofMaterial.SLATE);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("8250.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1237.50"), roof.getTaxAmount());
        assertEquals(new BigDecimal("9487.50"), roof.getTotalPrice());
    }

    @Test
    void roofingSyntheticFactorApplied() {
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("100"));
        roof.setAreaSqFt(new BigDecimal("100"));
        roof.setMaterialCostPerSqFt(new BigDecimal("10"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(1);
        roof.setRoofPitch(BigDecimal.ONE);
        roof.setRoofMaterial(RoofMaterial.SYNTHETIC);

        service.calculateEstimate(roof);

        assertEquals(new BigDecimal("8250.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1237.50"), roof.getTaxAmount());
        assertEquals(new BigDecimal("9487.50"), roof.getTotalPrice());
    }

    // ==================== WindowDoorReplace Tests ====================

    @Test
    void windowDoorReplaceCalculatesWithBothWindowsAndDoors() {
        WindowDoorReplace windowDoor = new WindowDoorReplace();
        windowDoor.setWindowType(WindowType.CASEMENT); // factor 1.0, base $800
        windowDoor.setDoorType(DoorType.WOOD); // factor 1.0, base $1200
        windowDoor.setWindowCount(3);
        windowDoor.setDoorCount(2);
        windowDoor.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(windowDoor);

        // Material: (3 * 800 * 1.0) + (2 * 1200 * 1.0) = 2400 + 2400 = 4800
        // Labor: (3 * 50 * 3) + (2 * 50 * 3) = 450 + 300 = 750
        // Base: 4800 + 750 = 5550
        // Overhead: 5550 * 0.15 = 832.50
        // Contingency: 5550 * 0.10 = 555
        // Estimate: 5550 + 832.50 + 555 = 6937.50
        // Tax: 6937.50 * 0.15 = 1040.63
        // Total: 6937.50 + 1040.63 = 7978.13
        assertEquals(new BigDecimal("6937.50"), windowDoor.getEstimatePrice());
        assertEquals(new BigDecimal("1040.63"), windowDoor.getTaxAmount());
        assertEquals(new BigDecimal("7978.13"), windowDoor.getTotalPrice());
    }

    @Test
    void windowDoorReplaceWithOnlyWindows() {
        WindowDoorReplace windowDoor = new WindowDoorReplace();
        windowDoor.setWindowType(WindowType.DOUBLE_HUNG); // factor 1.05
        windowDoor.setDoorType(null);
        windowDoor.setWindowCount(5);
        windowDoor.setDoorCount(0);
        windowDoor.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(windowDoor);

        // Material: 5 * 800 * 1.05 = 4200
        // Labor: 5 * 50 * 3 = 750
        // Base: 4950
        // Overhead: 742.50, Contingency: 495
        // Estimate: 6187.50
        assertEquals(new BigDecimal("6187.50"), windowDoor.getEstimatePrice());
        assertEquals(new BigDecimal("928.13"), windowDoor.getTaxAmount());
        assertEquals(new BigDecimal("7115.63"), windowDoor.getTotalPrice());
    }

    @Test
    void windowDoorReplaceWithOnlyDoors() {
        WindowDoorReplace windowDoor = new WindowDoorReplace();
        windowDoor.setWindowType(null);
        windowDoor.setDoorType(DoorType.FIBERGLASS); // factor 1.15
        windowDoor.setWindowCount(0);
        windowDoor.setDoorCount(3);
        windowDoor.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(windowDoor);

        // Material: 3 * 1200 * 1.15 = 4140
        // Labor: 3 * 50 * 3 = 450
        // Base: 4590
        // Overhead: 688.50, Contingency: 459
        // Estimate: 5737.50
        assertEquals(new BigDecimal("5737.50"), windowDoor.getEstimatePrice());
        assertEquals(new BigDecimal("860.63"), windowDoor.getTaxAmount());
        assertEquals(new BigDecimal("6598.13"), windowDoor.getTotalPrice());
    }

    @Test
    void windowDoorReplaceAppliesSliderWindowFactor() {
        WindowDoorReplace windowDoor = new WindowDoorReplace();
        windowDoor.setWindowType(WindowType.SLIDER); // factor 0.95
        windowDoor.setDoorType(null);
        windowDoor.setWindowCount(4);
        windowDoor.setDoorCount(0);
        windowDoor.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(windowDoor);

        // Material: 4 * 800 * 0.95 = 3040
        // Labor: 4 * 50 * 3 = 600
        // Base: 3640
        assertEquals(new BigDecimal("4550.00"), windowDoor.getEstimatePrice());
        assertEquals(new BigDecimal("682.50"), windowDoor.getTaxAmount());
        assertEquals(new BigDecimal("5232.50"), windowDoor.getTotalPrice());
    }

    @Test
    void windowDoorReplaceAppliesGlassPanelDoorFactor() {
        WindowDoorReplace windowDoor = new WindowDoorReplace();
        windowDoor.setWindowType(null);
        windowDoor.setDoorType(DoorType.GLASS_PANEL); // factor 1.30
        windowDoor.setWindowCount(0);
        windowDoor.setDoorCount(2);
        windowDoor.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(windowDoor);

        // Material: 2 * 1200 * 1.30 = 3120
        // Labor: 2 * 50 * 3 = 300
        // Base: 3420
        assertEquals(new BigDecimal("4275.00"), windowDoor.getEstimatePrice());
        assertEquals(new BigDecimal("641.25"), windowDoor.getTaxAmount());
        assertEquals(new BigDecimal("4916.25"), windowDoor.getTotalPrice());
    }

    // ==================== DeckPatioAddition Tests ====================

    @Test
    void deckPatioAdditionBasicCalculation() {
        DeckPatioAddition deckPatio = new DeckPatioAddition();
        deckPatio.setDeckMaterial(DeckMaterial.WOOD); // factor 1.0
        deckPatio.setAreaSqFt(200.0);
        deckPatio.setHasRailing(false);
        deckPatio.setStairsCount(0);
        deckPatio.setIsCovered(false);
        deckPatio.setLocationFactor(BigDecimal.ONE);
        deckPatio.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(deckPatio);

        // Material: 200 * 25 * 1.0 = 5000
        // Labor: 200 * 50 = 10000
        // Base: 15000
        // Overhead: 2250, Contingency: 1500
        // Estimate: 18750
        assertEquals(new BigDecimal("18750.00"), deckPatio.getEstimatePrice());
        assertEquals(new BigDecimal("2812.50"), deckPatio.getTaxAmount());
        assertEquals(new BigDecimal("21562.50"), deckPatio.getTotalPrice());
    }

    @Test
    void deckPatioAdditionWithAllFeatures() {
        DeckPatioAddition deckPatio = new DeckPatioAddition();
        deckPatio.setDeckMaterial(DeckMaterial.COMPOSITE); // factor 1.25
        deckPatio.setAreaSqFt(300.0);
        deckPatio.setHasRailing(true);
        deckPatio.setStairsCount(2); // 2 * $500 = $1000
        deckPatio.setIsCovered(true); // 300 * $15 = $4500
        deckPatio.setLocationFactor(new BigDecimal("1.10"));
        deckPatio.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(deckPatio);

        assertEquals(new BigDecimal("44888.53"), deckPatio.getEstimatePrice());
        assertEquals(new BigDecimal("6733.28"), deckPatio.getTaxAmount());
        assertEquals(new BigDecimal("51621.80"), deckPatio.getTotalPrice());
    }

    @Test
    void deckPatioAdditionAppliesPvcFactor() {
        DeckPatioAddition deckPatio = new DeckPatioAddition();
        deckPatio.setDeckMaterial(DeckMaterial.PVC); // factor 1.40
        deckPatio.setAreaSqFt(150.0);
        deckPatio.setHasRailing(false);
        deckPatio.setStairsCount(0);
        deckPatio.setIsCovered(false);
        deckPatio.setLocationFactor(BigDecimal.ONE);
        deckPatio.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(deckPatio);

        // Material: 150 * 25 * 1.40 = 5250
        // Labor: 150 * 50 = 7500
        // Base: 12750
        assertEquals(new BigDecimal("15937.50"), deckPatio.getEstimatePrice());
        assertEquals(new BigDecimal("2390.63"), deckPatio.getTaxAmount());
        assertEquals(new BigDecimal("18328.13"), deckPatio.getTotalPrice());
    }

    @Test
    void deckPatioAdditionAppliesAluminumFactor() {
        DeckPatioAddition deckPatio = new DeckPatioAddition();
        deckPatio.setDeckMaterial(DeckMaterial.ALUMINUM); // factor 1.50
        deckPatio.setAreaSqFt(100.0);
        deckPatio.setHasRailing(false);
        deckPatio.setStairsCount(1); // $500
        deckPatio.setIsCovered(false);
        deckPatio.setLocationFactor(BigDecimal.ONE);
        deckPatio.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(deckPatio);

        // Material: 100 * 25 * 1.50 = 3750
        // Labor: 100 * 50 = 5000
        // Stairs: 500
        // Base: 9250
        assertEquals(new BigDecimal("11562.50"), deckPatio.getEstimatePrice());
        assertEquals(new BigDecimal("1734.38"), deckPatio.getTaxAmount());
        assertEquals(new BigDecimal("13296.88"), deckPatio.getTotalPrice());
    }

    // ==================== FloorReplace Tests ====================

    @Test
    void floorReplaceBasicCalculation() {
        FloorReplace floorReplace = new FloorReplace();
        floorReplace.setSquareFeet(new BigDecimal("500"));
        floorReplace.setMaterialCostPerSqFt(new BigDecimal("8")); // HARDWOOD base price
        floorReplace.setExistingFloorMaterial(FlooringMaterial.CARPET); // removal factor 0.60
        floorReplace.setNewFloorMaterial(FlooringMaterial.HARDWOOD); // factor 1.0
        floorReplace.setSubfloorRepairNeeded(false);
        floorReplace.setLocationFactor(BigDecimal.ONE);
        floorReplace.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(floorReplace);

        // Material: 500 * 8 * 1.0 = 4000
        // Labor: 500 * 50 = 25000
        // Removal: 500 * 2 * 0.60 = 600
        // Base: 29600
        // Overhead: 4440, Contingency: 2960
        // Estimate: 37000
        assertEquals(new BigDecimal("37000.00"), floorReplace.getEstimatePrice());
        assertEquals(new BigDecimal("5550.00"), floorReplace.getTaxAmount());
        assertEquals(new BigDecimal("42550.00"), floorReplace.getTotalPrice());
    }

    @Test
    void floorReplaceWithSubfloorRepair() {
        FloorReplace floorReplace = new FloorReplace();
        floorReplace.setSquareFeet(new BigDecimal("200"));
        floorReplace.setMaterialCostPerSqFt(new BigDecimal("6")); // ENGINEERED_HARDWOOD
        floorReplace.setExistingFloorMaterial(FlooringMaterial.VINYL); // removal factor 0.70
        floorReplace.setNewFloorMaterial(FlooringMaterial.ENGINEERED_HARDWOOD); // factor 0.85
        floorReplace.setSubfloorRepairNeeded(true); // 200 * 3.50 = 700
        floorReplace.setLocationFactor(BigDecimal.ONE);
        floorReplace.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(floorReplace);

        // Material: 200 * 6 * 0.85 = 1020
        // Labor: 200 * 50 = 10000
        // Subfloor: 200 * 3.50 = 700
        // Removal: 200 * 2 * 0.70 = 280
        // Base: 12000
        assertEquals(new BigDecimal("15225.00"), floorReplace.getEstimatePrice());
        assertEquals(new BigDecimal("2283.75"), floorReplace.getTaxAmount());
        assertEquals(new BigDecimal("17508.75"), floorReplace.getTotalPrice());
    }

    @Test
    void floorReplaceAppliesLaminateFactor() {
        FloorReplace floorReplace = new FloorReplace();
        floorReplace.setSquareFeet(new BigDecimal("300"));
        floorReplace.setMaterialCostPerSqFt(new BigDecimal("3")); // LAMINATE
        floorReplace.setExistingFloorMaterial(FlooringMaterial.LAMINATE); // removal factor 0.80
        floorReplace.setNewFloorMaterial(FlooringMaterial.LAMINATE); // factor 0.60
        floorReplace.setSubfloorRepairNeeded(false);
        floorReplace.setLocationFactor(BigDecimal.ONE);
        floorReplace.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(floorReplace);

        // Material: 300 * 3 * 0.60 = 540
        // Labor: 300 * 50 = 15000
        // Removal: 300 * 2 * 0.80 = 480
        // Base: 16020
        assertEquals(new BigDecimal("20475.00"), floorReplace.getEstimatePrice());
        assertEquals(new BigDecimal("3071.25"), floorReplace.getTaxAmount());
        assertEquals(new BigDecimal("23546.25"), floorReplace.getTotalPrice());
    }

    @Test
    void floorReplaceRemovingTile() {
        FloorReplace floorReplace = new FloorReplace();
        floorReplace.setSquareFeet(new BigDecimal("150"));
        floorReplace.setMaterialCostPerSqFt(new BigDecimal("5")); // TILE
        floorReplace.setExistingFloorMaterial(FlooringMaterial.TILE); // removal factor 1.50 (difficult)
        floorReplace.setNewFloorMaterial(FlooringMaterial.TILE); // factor 0.90
        floorReplace.setSubfloorRepairNeeded(true);
        floorReplace.setLocationFactor(new BigDecimal("1.05"));
        floorReplace.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(floorReplace);

        // Material: 150 * 5 * 0.90 = 675
        // Labor: 150 * 50 = 7500
        // Subfloor: 150 * 3.50 = 525
        // Removal: 150 * 2 * 1.50 = 450
        // Base before location: 9150
        // Location: 9150 * 1.05 = 9607.50
        // Overhead: 1441.13, Contingency: 960.75
        // Estimate: 12009.38
        assertEquals(new BigDecimal("12107.81"), floorReplace.getEstimatePrice());
        assertEquals(new BigDecimal("1816.17"), floorReplace.getTaxAmount());
        assertEquals(new BigDecimal("13923.98"), floorReplace.getTotalPrice());
    }

    @Test
    void floorReplaceAppliesVinylFactor() {
        FloorReplace floorReplace = new FloorReplace();
        floorReplace.setSquareFeet(new BigDecimal("400"));
        floorReplace.setMaterialCostPerSqFt(new BigDecimal("2.50")); // VINYL
        floorReplace.setExistingFloorMaterial(FlooringMaterial.HARDWOOD); // removal factor 1.20
        floorReplace.setNewFloorMaterial(FlooringMaterial.VINYL); // factor 0.50
        floorReplace.setSubfloorRepairNeeded(false);
        floorReplace.setLocationFactor(BigDecimal.ONE);
        floorReplace.setTaxRate(new BigDecimal("0.15"));

        service.calculateEstimate(floorReplace);

        // Material: 400 * 2.50 * 0.50 = 500
        // Labor: 400 * 50 = 20000
        // Removal: 400 * 2 * 1.20 = 960
        // Base: 21460
        assertEquals(new BigDecimal("27450.00"), floorReplace.getEstimatePrice());
        assertEquals(new BigDecimal("4117.50"), floorReplace.getTaxAmount());
        assertEquals(new BigDecimal("31567.50"), floorReplace.getTotalPrice());
    }
}






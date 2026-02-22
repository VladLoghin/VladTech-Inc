package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.EstimateSettings;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.floor.FloorReplace;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.patio.DeckPatioAddition;
import org.example.vladtech.estimates.data.patio.DeckMaterial;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.data.shared.FlooringMaterial;
import org.example.vladtech.estimates.data.windowanddoor.WindowDoorReplace;
import org.example.vladtech.estimates.data.windowanddoor.WindowType;
import org.example.vladtech.estimates.data.windowanddoor.DoorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EstimationServiceImplCoverageTest {

    private EstimationServiceImpl service;

    @Mock
    private EstimateSettingsService estimateSettingsService;

    @BeforeEach
    void setUp() {
        EstimateSettings settings = EstimateSettings.defaultSettings();
        lenient().when(estimateSettingsService.getSettings()).thenReturn(settings);
        service = new EstimationServiceImpl(estimateSettingsService);
    }

    @Test
    void resolveSidingMaterialFactors_AllEnums() {
        for (SidingMaterial mat : SidingMaterial.values()) {
            SidingReplace s = new SidingReplace();
            s.setSquareFeet(new BigDecimal("10"));
            s.setMaterialCostPerSqFt(null); // force using settings lookup
            s.setLocationFactor(BigDecimal.ONE);
            s.setStories(1);
            s.setIncludeInsulation(false);
            s.setSidingMaterial(mat);

            RenovationProject r = service.calculateEstimate(s);
            assertNotNull(r.getEstimatePrice(), "estimatePrice should be set for " + mat);
        }
    }

    @Test
    void resolveRoofMaterialFactors_AllEnums() {
        for (RoofMaterial mat : RoofMaterial.values()) {
            RoofingReplace r = new RoofingReplace();
            r.setSquareFeet(new BigDecimal("20"));
            r.setAreaSqFt(new BigDecimal("20"));
            r.setMaterialCostPerSqFt(null);
            r.setLocationFactor(BigDecimal.ONE);
            r.setStories(1);
            r.setRoofPitch(BigDecimal.ONE);
            r.setRoofMaterial(mat);

            RenovationProject out = service.calculateEstimate(r);
            assertNotNull(out.getEstimatePrice(), "estimatePrice should be set for " + mat);
        }
    }

    @Test
    void resolveDeckMaterial_AllEnums() {
        for (DeckMaterial dm : DeckMaterial.values()) {
            DeckPatioAddition d = new DeckPatioAddition();
            d.setAreaSqFt(50.0);
            d.setDeckMaterial(dm);
            d.setLocationFactor(BigDecimal.ONE);
            d.setHasRailing(false);
            d.setStairsCount(0);
            d.setIsCovered(false);
            d.setTaxRate(new BigDecimal("0.15"));

            RenovationProject out = service.calculateEstimate(d);
            assertNotNull(out.getEstimatePrice(), "estimatePrice should be set for " + dm);
        }
    }

    @Test
    void resolveFlooringMaterialAndRemoval_AllEnums() {
        for (FlooringMaterial fm : FlooringMaterial.values()) {
            FloorReplace f = new FloorReplace();
            f.setSquareFeet(new BigDecimal("30"));
            f.setMaterialCostPerSqFt(null); // force settings
            f.setExistingFloorMaterial(fm);
            f.setNewFloorMaterial(fm);
            f.setSubfloorRepairNeeded(false);
            f.setLocationFactor(BigDecimal.ONE);
            f.setTaxRate(new BigDecimal("0.15"));

            RenovationProject out = service.calculateEstimate(f);
            assertNotNull(out.getEstimatePrice(), "estimatePrice should be set for " + fm);
        }
    }

    @Test
    void resolveWindowAndDoorMaterial_AllEnums() {
        for (WindowType wt : WindowType.values()) {
            WindowDoorReplace w = new WindowDoorReplace();
            w.setWindowType(wt);
            w.setDoorType(null);
            w.setWindowCount(1);
            w.setDoorCount(0);
            w.setTaxRate(new BigDecimal("0.15"));

            RenovationProject out = service.calculateEstimate(w);
            assertNotNull(out.getEstimatePrice(), "estimatePrice should be set for window " + wt);
        }

        for (DoorType dt : DoorType.values()) {
            WindowDoorReplace w = new WindowDoorReplace();
            w.setWindowType(null);
            w.setDoorType(dt);
            w.setWindowCount(0);
            w.setDoorCount(1);
            w.setTaxRate(new BigDecimal("0.15"));

            RenovationProject out = service.calculateEstimate(w);
            assertNotNull(out.getEstimatePrice(), "estimatePrice should be set for door " + dt);
        }
    }

    @Test
    void calculateEstimate_genericPaths_exerciseExtraBranches() {
        // test insulation adder path
        SidingReplace s = new SidingReplace();
        s.setSquareFeet(new BigDecimal("10"));
        s.setMaterialCostPerSqFt(new BigDecimal("1"));
        s.setLocationFactor(BigDecimal.ONE);
        s.setStories(3);
        s.setIncludeInsulation(true);
        s.setSidingMaterial(SidingMaterial.VINYL);
        RenovationProject r1 = service.calculateEstimate(s);
        assertNotNull(r1.getEstimatePrice());

        // test kitchen plumbing/electrical
        KitchenRemodel k = new KitchenRemodel();
        k.setSquareFeet(new BigDecimal("10"));
        k.setMaterialCostPerSqFt(new BigDecimal("10"));
        k.setLocationFactor(BigDecimal.ONE);
        k.setApplianceAllowance(1000.0);
        k.setPlumbingChanges(true);
        k.setElectricalChanges(true);
        RenovationProject r2 = service.calculateEstimate(k);
        assertNotNull(r2.getEstimatePrice());

        // test roof tear off and skylight
        RoofingReplace roof = new RoofingReplace();
        roof.setSquareFeet(new BigDecimal("10"));
        roof.setAreaSqFt(new BigDecimal("10"));
        roof.setMaterialCostPerSqFt(new BigDecimal("5"));
        roof.setLocationFactor(BigDecimal.ONE);
        roof.setStories(2);
        roof.setRoofPitch(new BigDecimal("2.0"));
        roof.setRoofMaterial(RoofMaterial.METAL);
        roof.setTearOffRequired(true);
        roof.setHasSkylights(true);
        roof.setNumSkylights(1);
        RenovationProject r3 = service.calculateEstimate(roof);
        assertNotNull(r3.getEstimatePrice());
    }
}


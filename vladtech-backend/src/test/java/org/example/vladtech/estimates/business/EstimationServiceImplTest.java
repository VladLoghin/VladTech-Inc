package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofMaterial;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.siding.SidingMaterial;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.exceptions.EstimationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EstimationServiceImplTest {

    private EstimationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EstimationServiceImpl();
        // Align with defaults used in production to make assertions predictable
        setField("laborRate", new BigDecimal("50.00"));
        setField("overheadRate", new BigDecimal("0.15"));
        setField("contingencyRate", new BigDecimal("0.10"));
        setField("taxRate", new BigDecimal("0.15"));

        setField("vinylFactor", BigDecimal.ONE);
        setField("woodFactor", new BigDecimal("1.10"));
        setField("fiberCementFactor", new BigDecimal("1.20"));
        setField("brickFactor", new BigDecimal("1.30"));
        setField("stoneVeneerFactor", new BigDecimal("1.45"));

        setField("asphaltFactor", BigDecimal.ONE);
        setField("metalFactor", new BigDecimal("1.20"));
        setField("clayFactor", new BigDecimal("1.50"));
        setField("slateFactor", new BigDecimal("1.80"));
        setField("syntheticFactor", new BigDecimal("1.30"));
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

        assertEquals(new BigDecimal("14504.38"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("2175.66"), roof.getTaxAmount());
        assertEquals(new BigDecimal("16680.03"), roof.getTotalPrice());
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

        assertEquals(new BigDecimal("12637.50"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1895.63"), siding.getTaxAmount());
        assertEquals(new BigDecimal("14533.13"), siding.getTotalPrice());
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

        assertEquals(new BigDecimal("7625.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1143.75"), siding.getTaxAmount());
        assertEquals(new BigDecimal("8768.75"), siding.getTotalPrice());
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

        assertEquals(new BigDecimal("7750.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1162.50"), siding.getTaxAmount());
        assertEquals(new BigDecimal("8912.50"), siding.getTotalPrice());
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

        assertEquals(new BigDecimal("7875.00"), siding.getEstimatePrice());
        assertEquals(new BigDecimal("1181.25"), siding.getTaxAmount());
        assertEquals(new BigDecimal("9056.25"), siding.getTotalPrice());
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

        assertEquals(new BigDecimal("8937.50"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1340.63"), roof.getTaxAmount());
        assertEquals(new BigDecimal("10278.13"), roof.getTotalPrice());
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

        assertEquals(new BigDecimal("9350.00"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1402.50"), roof.getTaxAmount());
        assertEquals(new BigDecimal("10752.50"), roof.getTotalPrice());
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

        assertEquals(new BigDecimal("8662.50"), roof.getEstimatePrice());
        assertEquals(new BigDecimal("1299.38"), roof.getTaxAmount());
        assertEquals(new BigDecimal("9961.88"), roof.getTotalPrice());
    }

    private void setField(String name, BigDecimal value) {
        try {
            Field f = EstimationServiceImpl.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
package org.example.vladtech.estimates.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "estimate_settings")
public class EstimateSettings {

    public static final String DEFAULT_ID = "default";

    @Id
    private String id;

    private BigDecimal laborRate;
    private BigDecimal overheadRate;
    private BigDecimal contingencyRate;
    private BigDecimal taxRate;

    private BigDecimal sidingExtraLaborPerStoryRate;
    private BigDecimal roofingExtraLaborPerStoryRate;
    private BigDecimal insulationAdderPerSqFt;
    private BigDecimal roofPitchFactorPerUnit;
    private BigDecimal roofTearOffCostPerSqFt;
    private BigDecimal roofSkylightCost;

    private BigDecimal kitchenPlumbingCost;
    private BigDecimal kitchenElectricalCost;

    private BigDecimal windowBaseCostPerUnit;
    private BigDecimal doorBaseCostPerUnit;
    private BigDecimal windowDoorLaborRateMultiplier;

    private BigDecimal deckBaseMaterialCostPerSqFt;
    private BigDecimal deckRailingCostPerLinearFoot;
    private BigDecimal deckStairsCost;
    private BigDecimal deckCoverCostPerSqFt;

    private BigDecimal floorSubfloorRepairCostPerSqFt;
    private BigDecimal floorRemovalBaseCostPerSqFt;

    private SidingFactors sidingFactors;
    private RoofFactors roofFactors;
    private WindowFactors windowFactors;
    private DoorFactors doorFactors;
    private DeckFactors deckFactors;
    private FlooringFactors flooringFactors;
    private FlooringRemovalFactors flooringRemovalFactors;

    public static EstimateSettings defaultSettings() {
        EstimateSettings settings = new EstimateSettings();
        settings.setId(DEFAULT_ID);

        settings.setLaborRate(new BigDecimal("50.00"));
        settings.setOverheadRate(new BigDecimal("0.15"));
        settings.setContingencyRate(new BigDecimal("0.10"));
        settings.setTaxRate(new BigDecimal("0.15"));

        settings.setSidingExtraLaborPerStoryRate(new BigDecimal("0.10"));
        settings.setRoofingExtraLaborPerStoryRate(new BigDecimal("0.15"));
        settings.setInsulationAdderPerSqFt(new BigDecimal("0.75"));
        settings.setRoofPitchFactorPerUnit(new BigDecimal("0.10"));
        settings.setRoofTearOffCostPerSqFt(new BigDecimal("1.50"));
        settings.setRoofSkylightCost(new BigDecimal("1000"));

        settings.setKitchenPlumbingCost(new BigDecimal("2000"));
        settings.setKitchenElectricalCost(new BigDecimal("1500"));

        settings.setWindowBaseCostPerUnit(new BigDecimal("800"));
        settings.setDoorBaseCostPerUnit(new BigDecimal("1200"));
        settings.setWindowDoorLaborRateMultiplier(new BigDecimal("3"));

        settings.setDeckBaseMaterialCostPerSqFt(new BigDecimal("25.00"));
        settings.setDeckRailingCostPerLinearFoot(new BigDecimal("40"));
        settings.setDeckStairsCost(new BigDecimal("500"));
        settings.setDeckCoverCostPerSqFt(new BigDecimal("15"));

        settings.setFloorSubfloorRepairCostPerSqFt(new BigDecimal("3.50"));
        settings.setFloorRemovalBaseCostPerSqFt(new BigDecimal("2.00"));

        settings.setSidingFactors(new SidingFactors(
                new BigDecimal("1.00"),
                new BigDecimal("1.10"),
                new BigDecimal("1.20"),
                new BigDecimal("1.30"),
                new BigDecimal("1.45")
        ));

        settings.setRoofFactors(new RoofFactors(
                new BigDecimal("1.00"),
                new BigDecimal("1.20"),
                new BigDecimal("1.50"),
                new BigDecimal("1.80"),
                new BigDecimal("1.30")
        ));

        settings.setWindowFactors(new WindowFactors(
                new BigDecimal("1.00"),
                new BigDecimal("0.95"),
                new BigDecimal("1.05"),
                new BigDecimal("1.10"),
                new BigDecimal("0.85")
        ));

        settings.setDoorFactors(new DoorFactors(
                new BigDecimal("1.00"),
                new BigDecimal("1.15"),
                new BigDecimal("1.05"),
                new BigDecimal("1.30")
        ));

        settings.setDeckFactors(new DeckFactors(
                new BigDecimal("1.00"),
                new BigDecimal("1.25"),
                new BigDecimal("1.40"),
                new BigDecimal("1.50")
        ));

        settings.setFlooringFactors(new FlooringFactors(
                new BigDecimal("1.00"),
                new BigDecimal("0.85"),
                new BigDecimal("0.60"),
                new BigDecimal("0.50"),
                new BigDecimal("0.90"),
                new BigDecimal("0.70"),
                new BigDecimal("0.95")
        ));

        settings.setFlooringRemovalFactors(new FlooringRemovalFactors(
                new BigDecimal("1.20"),
                new BigDecimal("1.10"),
                new BigDecimal("0.80"),
                new BigDecimal("0.70"),
                new BigDecimal("1.50"),
                new BigDecimal("0.60"),
                new BigDecimal("0.50")
        ));

        return settings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SidingFactors {
        private BigDecimal vinyl;
        private BigDecimal wood;
        private BigDecimal fiberCement;
        private BigDecimal brick;
        private BigDecimal stoneVeneer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoofFactors {
        private BigDecimal asphalt;
        private BigDecimal metal;
        private BigDecimal clay;
        private BigDecimal slate;
        private BigDecimal synthetic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WindowFactors {
        private BigDecimal casement;
        private BigDecimal slider;
        private BigDecimal doubleHung;
        private BigDecimal awning;
        private BigDecimal fixed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoorFactors {
        private BigDecimal wood;
        private BigDecimal fiberglass;
        private BigDecimal steel;
        private BigDecimal glassPanel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeckFactors {
        private BigDecimal wood;
        private BigDecimal composite;
        private BigDecimal pvc;
        private BigDecimal aluminum;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlooringFactors {
        private BigDecimal hardwood;
        private BigDecimal engineeredHardwood;
        private BigDecimal laminate;
        private BigDecimal vinyl;
        private BigDecimal tile;
        private BigDecimal carpet;
        private BigDecimal polishedConcrete;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlooringRemovalFactors {
        private BigDecimal hardwood;
        private BigDecimal engineeredHardwood;
        private BigDecimal laminate;
        private BigDecimal vinyl;
        private BigDecimal tile;
        private BigDecimal carpet;
        private BigDecimal polishedConcrete;
    }
}

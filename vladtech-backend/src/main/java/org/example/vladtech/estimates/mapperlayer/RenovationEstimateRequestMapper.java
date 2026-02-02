package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.siding.SidingReplace;
import org.example.vladtech.estimates.data.windowanddoor.WindowDoorReplace;
import org.example.vladtech.estimates.data.patio.DeckPatioAddition;
import org.example.vladtech.estimates.data.floor.FloorReplace;
import org.example.vladtech.estimates.presentation.RenovationEstimateRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RenovationEstimateRequestMapper {

    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    RenovationProject toEntity(RenovationEstimateRequestModel request);

    @Mapping(target = "roofPitch", source = "roofPitch")
    @Mapping(target = "roofMaterial", source = "roofMaterial")
    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "areaSqFt", ignore = true)
    RoofingReplace toRoofingReplace(RenovationEstimateRequestModel request);

    @Mapping(target = "stories", source = "stories")
    @Mapping(target = "includeInsulation", source = "includeInsulation")
    @Mapping(target = "sidingMaterial", source = "sidingMaterial")
    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "areaSqFt", ignore = true)
    SidingReplace toSidingReplace(RenovationEstimateRequestModel request);

    @Mapping(target = "applianceAllowance", source = "applianceAllowance")
    @Mapping(target = "plumbingChanges", source = "plumbingChanges")
    @Mapping(target = "electricalChanges", source = "electricalChanges")
    @Mapping(target = "flooringMaterial", source = "flooringMaterial")
    @Mapping(target = "cabinetQuality", source = "cabinetQuality")
    @Mapping(target = "countertopMaterial", source = "countertopMaterial")
    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "areaSqFt", ignore = true)
    KitchenRemodel toKitchenRemodel(RenovationEstimateRequestModel request);

    @Mapping(target = "windowType", source = "windowType")
    @Mapping(target = "doorType", source = "doorType")
    @Mapping(target = "windowCount", source = "windowCount")
    @Mapping(target = "doorCount", source = "doorCount")
    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    WindowDoorReplace toWindowDoorReplace(RenovationEstimateRequestModel request);

    @Mapping(target = "deckMaterial", source = "deckMaterial")
    @Mapping(target = "hasRailing", source = "hasRailing")
    @Mapping(target = "stairsCount", source = "stairsCount")
    @Mapping(target = "isCovered", source = "isCovered")
    @Mapping(target = "areaSqFt", source = "areaSqFt")
    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    DeckPatioAddition toDeckPatioAddition(RenovationEstimateRequestModel request);

    @Mapping(target = "existingFloorMaterial", source = "existingFloorMaterial")
    @Mapping(target = "newFloorMaterial", source = "newFloorMaterial")
    @Mapping(target = "subfloorRepairNeeded", source = "subfloorRepairNeeded")
    @Mapping(target = "laborRate", ignore = true)
    @Mapping(target = "overheadRate", ignore = true)
    @Mapping(target = "contingencyRate", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "estimatePrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    FloorReplace toFloorReplace(RenovationEstimateRequestModel request);
}
package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.kitchen.KitchenRemodel;
import org.example.vladtech.estimates.data.roof.RoofingReplace;
import org.example.vladtech.estimates.data.siding.SidingReplace;
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
    RoofingReplace toRoofingReplace(RenovationEstimateRequestModel request);

    @Mapping(target = "stories", source = "stories")
    @Mapping(target = "includeInsulation", source = "includeInsulation")
    @Mapping(target = "sidingMaterial", source = "sidingMaterial")
    SidingReplace toSidingReplace(RenovationEstimateRequestModel request);

    @Mapping(target = "applianceAllowance", source = "applianceAllowance")
    @Mapping(target = "plumbingChanges", source = "plumbingChanges")
    @Mapping(target = "electricalChanges", source = "electricalChanges")
    @Mapping(target = "flooringMaterial", source = "flooringMaterial")
    @Mapping(target = "cabinetQuality", source = "cabinetQuality")
    @Mapping(target = "countertopMaterial", source = "countertopMaterial")
    KitchenRemodel toKitchenRemodel(RenovationEstimateRequestModel request);
}
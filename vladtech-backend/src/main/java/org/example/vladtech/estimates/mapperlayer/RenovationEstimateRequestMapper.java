package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.presentation.RenovationEstimateRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

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
}
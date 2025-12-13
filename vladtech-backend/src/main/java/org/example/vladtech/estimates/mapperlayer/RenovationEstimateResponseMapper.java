package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.presentation.RenovationEstimateResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = "spring")
public interface RenovationEstimateResponseMapper {

    RenovationEstimateResponseModel toResponse(RenovationProject project);
}
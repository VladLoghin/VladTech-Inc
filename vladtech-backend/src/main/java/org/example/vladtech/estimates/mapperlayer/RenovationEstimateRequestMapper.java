package org.example.vladtech.estimates.mapperlayer;

import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.presentation.RenovationEstimateRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RenovationEstimateRequestMapper {

    RenovationProject toEntity(RenovationEstimateRequestModel request);
}
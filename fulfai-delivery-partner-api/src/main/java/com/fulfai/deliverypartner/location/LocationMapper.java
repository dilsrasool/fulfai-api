package com.fulfai.deliverypartner.location;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface LocationMapper {

    LocationResponseDTO toResponseDTO(DriverLocation entity);
}

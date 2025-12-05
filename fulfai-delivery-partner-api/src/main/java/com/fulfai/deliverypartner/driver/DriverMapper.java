package com.fulfai.deliverypartner.driver;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface DriverMapper {

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLatitude", ignore = true)
    @Mapping(target = "lastLongitude", ignore = true)
    @Mapping(target = "lastLocationUpdate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Driver toEntity(DriverRequestDTO dto);

    DriverResponseDTO toResponseDTO(Driver entity);
}

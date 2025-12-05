package com.fulfai.deliverypartner.assignment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface AssignmentMapper {

    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "pickedUpAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DriverOrderAssignment toEntity(AssignmentRequestDTO dto);

    AssignmentResponseDTO toResponseDTO(DriverOrderAssignment entity);
}

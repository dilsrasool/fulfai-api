package com.fulfai.sellingpartner.companyJoinRequest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "cdi",
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CompanyJoinRequestMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CompanyJoinRequestResponseDTO toResponseDTO(CompanyJoinRequest entity);
}

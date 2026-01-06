package com.fulfai.sellingpartner.companyJoinRequest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "cdi",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CompanyJoinRequestMapper {

    @Mapping(target = "companyId", source = "companyId")
    @Mapping(target = "requestId", source = "requestId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "requestedAt", source = "requestedAt")
    @Mapping(target = "reviewedAt", source = "reviewedAt")
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CompanyJoinRequestResponseDTO toResponseDTO(CompanyJoinRequest entity);
}

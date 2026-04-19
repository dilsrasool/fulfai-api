package com.fulfai.sellingpartner.branch;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface BranchMapper {
    BranchMapper INSTANCE = Mappers.getMapper(BranchMapper.class);

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "geoHash5", ignore = true)
    @Mapping(target = "geoHash6", ignore = true)
    @Mapping(target = "ratingAverage", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "ratingSum", ignore = true)
    @Mapping(target = "locationUpdatedAt", ignore = true)
    Branch toEntity(BranchRequestDTO dto);

    BranchResponseDTO toResponseDTO(Branch entity);
}

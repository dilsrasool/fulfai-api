package com.fulfai.sellingpartner.company;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerSub", ignore = true)     // generated internally
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyRequestDTO dto);

    // users list is not mapped here; service layer will populate it
    @Mapping(target = "users", ignore = true)
    CompanyResponseDTO toResponseDTO(Company entity);
}

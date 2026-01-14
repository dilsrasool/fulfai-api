package com.fulfai.sellingpartner.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    // ---------- Request → Entity ----------
    @Mapping(target = "companyId", ignore = true)        // set from security / context
    @Mapping(target = "categoryId", ignore = true)       // generated in service (UUID / ULID)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequestDTO dto);

    // ---------- Entity → Response ----------
    CategoryResponseDTO toResponseDTO(Category entity);
}

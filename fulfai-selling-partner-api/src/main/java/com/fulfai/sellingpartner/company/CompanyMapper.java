package com.fulfai.sellingpartner.company;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "cdi")
public interface CompanyMapper {

    /* =========================
       REQUEST → ENTITY
    ========================== */

    @Mappings({
        // 🔒 System-managed fields
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "joinCode", ignore = true),
        @Mapping(target = "ownerSub", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),

    })
    Company toEntity(CompanyRequestDTO dto);

    /* =========================
       ENTITY → RESPONSE
    ========================== */

    @Mappings({
        // ✅ Explicit alias for frontend clarity
        @Mapping(source = "id", target = "companyGuid")
        // joinCode, phoneNumber, state, etc map automatically
    })
    CompanyResponseDTO toResponseDTO(Company entity);
}

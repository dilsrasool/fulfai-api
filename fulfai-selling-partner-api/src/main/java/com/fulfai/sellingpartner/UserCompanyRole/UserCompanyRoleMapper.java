package com.fulfai.sellingpartner.UserCompanyRole;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface UserCompanyRoleMapper {

    /* ============================
       DTO → ENTITY
       companyBranch is derived
    ============================= */

    @Mapping(target = "companyBranch", ignore = true)
    UserCompanyRole toEntity(UserCompanyRoleRequestDTO dto);

    /* ============================
       ENTITY → DTO
    ============================= */

    UserCompanyRoleResponseDTO toResponseDTO(UserCompanyRole entity);

    /* ============================
       AFTER MAPPING
       Build composite keys safely
    ============================= */

    @AfterMapping
    default void buildCompanyBranch(
            UserCompanyRoleRequestDTO dto,
            @MappingTarget UserCompanyRole entity
    ) {
        entity.setCompanyAndBranch(
                dto.getCompanyId(),
                dto.getBranchId()   // null = company-level role
        );
    }
}

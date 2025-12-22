package com.fulfai.sellingpartner.UserCompanyRole;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface UserCompanyRoleMapper {

    // Request DTO → Entity
    UserCompanyRole toEntity(UserCompanyRoleRequestDTO dto);

    // Entity → Response DTO
    UserCompanyRoleResponseDTO toResponseDTO(UserCompanyRole entity);
}

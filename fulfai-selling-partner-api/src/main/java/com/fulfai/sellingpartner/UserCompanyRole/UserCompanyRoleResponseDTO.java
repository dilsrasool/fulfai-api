package com.fulfai.sellingpartner.UserCompanyRole;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class UserCompanyRoleResponseDTO {

    private String userId;

    private String companyId;   // 👈 added to match entity/schema

    private String role;
}



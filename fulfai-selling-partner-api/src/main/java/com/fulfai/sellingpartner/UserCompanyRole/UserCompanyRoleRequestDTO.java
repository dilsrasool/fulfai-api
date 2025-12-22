package com.fulfai.sellingpartner.UserCompanyRole;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class UserCompanyRoleRequestDTO {

    @NotBlank(message = "UserId cannot be blank")
    @Size(max = 100, message = "UserId must be less than 100 characters")
    private String userId;

    @NotBlank(message = "CompanyId cannot be blank")
    @Size(max = 100, message = "CompanyId must be less than 100 characters")
    private String companyId;   // 👈 added to match entity/schema

    @NotBlank(message = "Role cannot be blank")
    @Size(max = 50, message = "Role must be less than 50 characters")
    private String role;
}

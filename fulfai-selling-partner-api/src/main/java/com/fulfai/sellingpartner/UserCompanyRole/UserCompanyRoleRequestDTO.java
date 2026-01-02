package com.fulfai.sellingpartner.UserCompanyRole;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class UserCompanyRoleRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    // ❌ NO validation here (server injected)
    private String companyId;

    // Optional
    @Size(max = 100)
    private String branchId;

    @NotBlank(message = "Role cannot be blank")
    @Size(max = 50)
    private String role;
}

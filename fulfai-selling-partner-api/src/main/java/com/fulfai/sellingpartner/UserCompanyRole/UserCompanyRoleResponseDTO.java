package com.fulfai.sellingpartner.UserCompanyRole;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class UserCompanyRoleResponseDTO {

    private String userId;          // internal (DO NOT show in UI)
    private String displayName;     // ✅ SAFE FOR UI
    private String companyId;
    private String branchId;
    private String role;
}

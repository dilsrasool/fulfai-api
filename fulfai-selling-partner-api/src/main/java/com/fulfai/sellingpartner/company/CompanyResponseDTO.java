package com.fulfai.sellingpartner.company;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleResponseDTO;

@Data
@RegisterForReflection
public class CompanyResponseDTO {

    /* =========================
       CORE IDENTIFIERS
    ========================== */

    /**
     * Primary company identifier (GUID).
     * Used internally and by APIs.
     */
    private String id;

    /**
     * Company join code shown to owners.
     * Used by users to request access.
     */
    private String joinCode;

    /**
     * Alias for frontend clarity (same value as id).
     */
    private String companyGuid;

    /* =========================
       COMPANY DETAILS
    ========================== */

    private String name;
    private String address;
    private String city;
    private String state;
    private String country;

    private String email;
    private String phoneNumber;

    private String licenseNo;
    private String trn;
    private String website;
    private String logo;

    private List<String> operatingCountries;

    /* =========================
       AUDIT
    ========================== */

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Cognito sub of company owner.
     */
    private String ownerSub;

    /* =========================
       USERS & ROLES
    ========================== */

    private List<UserCompanyRoleResponseDTO> users;
}

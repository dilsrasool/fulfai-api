package com.fulfai.sellingpartner.company;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleResponseDTO;

@Data
@RegisterForReflection
public class CompanyResponseDTO {

    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String email;
    private String licenseNo;
    private String logo;
    private String phoneNumber;
    private String trn;
    private String website;
    private List<String> operatingCountries;
    private Instant createdAt;
    private Instant updatedAt;
    private String ownerSub;

    /**
     * 👥 Users with their roles.
     * Populated in the service layer by querying UserCompanyRoleRepository,
     * not stored directly in the Company entity.
     */
    private List<UserCompanyRoleResponseDTO> users;
}

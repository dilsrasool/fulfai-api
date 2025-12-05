package com.fulfai.partner.branch;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchResponseDTO {

    private String companyId;
    private String branchId;
    private String name;
    private String address;
    private String city;
    private String country;
    private String phoneNumber;
    private String email;
    private String managerName;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}

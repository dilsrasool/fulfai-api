package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CompanyJoinRequestResponseDTO {

    private String requestId;
    private String companyId;
    private String userId;
    private String status;

    private Instant requestedAt;
    private Instant reviewedAt;
    private String reviewedBy;

    private Instant createdAt;
    private Instant updatedAt;
}

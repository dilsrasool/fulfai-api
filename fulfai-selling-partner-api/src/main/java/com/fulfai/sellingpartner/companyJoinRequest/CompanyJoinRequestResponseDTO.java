package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CompanyJoinRequestResponseDTO {

    /** Unique request identifier */
    private String requestId;

    /** Company being joined */
    private String companyId;

    /** Cognito user sub of requester */
    private String userId;

    /** PENDING | APPROVED | REJECTED */
    private String status;

    /** When the join request was created */
    private Instant requestedAt;

    /** When owner reviewed the request */
    private Instant reviewedAt;

    /** Cognito sub of reviewer (OWNER) */
    private String reviewedBy;

    /** Audit fields */
    private Instant createdAt;
    private Instant updatedAt;
}

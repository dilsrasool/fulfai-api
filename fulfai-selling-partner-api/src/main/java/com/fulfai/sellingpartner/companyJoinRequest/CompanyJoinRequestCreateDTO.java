package com.fulfai.sellingpartner.companyJoinRequest;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CompanyJoinRequestCreateDTO {

    /**
     * Cognito user sub of the requester
     */
    private String userId;
}

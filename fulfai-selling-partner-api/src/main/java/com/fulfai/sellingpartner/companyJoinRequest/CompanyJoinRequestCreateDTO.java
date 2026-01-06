package com.fulfai.sellingpartner.companyJoinRequest;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class CompanyJoinRequestCreateDTO {

    /**
     * Company GUID (primary ID from Company table)
     * User pastes this to request joining
     */
    @NotBlank(message = "Company ID (GUID) is required")
    private String companyId;

    /**
     * Optional message to the company owner
     */
    @Size(max = 250, message = "Message must be less than 250 characters")
    private String message;
}

package com.fulfai.sellingpartner.UserCompanyRole;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

@Data
@DynamoDbBean
@RegisterForReflection
public class UserCompanyRole {

    private String userId;
    private String companyId;   // 👈 new field
    private String role;

    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbAttribute("role")
    public String getRole() {
        return role;
    }
}

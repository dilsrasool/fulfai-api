package com.fulfai.sellingpartner.company;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
@RegisterForReflection
public class Company {

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

    // 🔑 Primary Key
    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    // ✅ GSI Partition Key
    @DynamoDbSecondaryPartitionKey(indexNames = "ownerSub-index")
    @DynamoDbAttribute("ownerSub")
    public String getOwnerSub() {
        return ownerSub;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("address")
    public String getAddress() {
        return address;
    }

    @DynamoDbAttribute("city")
    public String getCity() {
        return city;
    }

    @DynamoDbAttribute("country")
    public String getCountry() {
        return country;
    }

    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("licenseNo")
    public String getLicenseNo() {
        return licenseNo;
    }

    @DynamoDbAttribute("logo")
    public String getLogo() {
        return logo;
    }

    @DynamoDbAttribute("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @DynamoDbAttribute("trn")
    public String getTrn() {
        return trn;
    }

    @DynamoDbAttribute("website")
    public String getWebsite() {
        return website;
    }

    @DynamoDbAttribute("operatingCountries")
    public List<String> getOperatingCountries() {
        return operatingCountries;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

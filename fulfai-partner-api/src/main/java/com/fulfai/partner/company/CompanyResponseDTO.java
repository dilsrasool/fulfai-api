package com.fulfai.partner.company;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

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
}

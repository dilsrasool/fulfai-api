package com.fulfai.deliverypartner.company;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class CompanyResponseDTO {

    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String email;
    private String phoneNumber;
    private String licenseNo;
    private String logo;
    private List<String> operatingCities;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.fulfai.deliverypartner.company;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String address;
    private String city;
    private String country;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String licenseNo;
    private String logo;
    private List<String> operatingCities;
    private Boolean isActive;
}

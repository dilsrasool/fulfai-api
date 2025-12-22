package com.fulfai.sellingpartner.company;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class CompanyRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 200, message = "Address must be less than 200 characters")
    private String address;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 50, message = "City must be less than 50 characters")
    private String city;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 50, message = "Country must be less than 50 characters")
    private String country;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(max = 50, message = "License number must be less than 50 characters")
    private String licenseNo;

    @Size(max = 255, message = "Logo URL must be less than 255 characters")
    private String logo;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;

    @Size(max = 50, message = "TRN must be less than 50 characters")
    private String trn;

    @Size(max = 100, message = "Website must be less than 100 characters")
    private String website;

    private List<String> operatingCountries;

    // ✅ Added: startDate with proper JSONB format
    // Accepts full ISO timestamps like "2025-11-19T00:00:00Z"
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant startDate;
}

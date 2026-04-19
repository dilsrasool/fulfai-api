package com.fulfai.sellingpartner.branch;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchRequestDTO {

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

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(max = 100, message = "Manager name must be less than 100 characters")
    private String managerName;

    private Double latitude;

    private Double longitude;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "regularOpeningTime must be in HH:mm format")
    private String regularOpeningTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "regularClosingTime must be in HH:mm format")
    private String regularClosingTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "dayOpeningTime must be in HH:mm format")
    private String dayOpeningTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "dayClosingTime must be in HH:mm format")
    private String dayClosingTime;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "dayScheduleDate must be in yyyy-MM-dd format")
    private String dayScheduleDate;

    private Boolean isActive;
}

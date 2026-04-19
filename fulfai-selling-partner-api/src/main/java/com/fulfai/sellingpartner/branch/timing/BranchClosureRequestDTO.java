package com.fulfai.sellingpartner.branch.timing;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchClosureRequestDTO {

    @NotBlank(message = "date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "date must be yyyy-MM-dd")
    private String date;

    private Boolean closedAllDay;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "openingTime must be HH:mm")
    private String openingTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "closingTime must be HH:mm")
    private String closingTime;

    @Size(max = 200, message = "reason must be less than 200 characters")
    private String reason;
}

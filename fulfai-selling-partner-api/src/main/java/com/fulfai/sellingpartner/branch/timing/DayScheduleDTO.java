package com.fulfai.sellingpartner.branch.timing;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class DayScheduleDTO {

    private Boolean open;
    private String openingTime;
    private String closingTime;
}

package com.fulfai.sellingpartner.branch.timing;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchTimingsUpdateRequestDTO {

    @NotBlank(message = "timezone is required")
    private String timezone;

    @JsonAlias({"businessHours"})
    @JsonDeserialize(using = WeeklyScheduleDeserializer.class)
    private Map<String, DayScheduleDTO> weeklySchedule;
}

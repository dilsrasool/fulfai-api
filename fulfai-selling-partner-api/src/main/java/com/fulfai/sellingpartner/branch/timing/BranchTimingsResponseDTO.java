package com.fulfai.sellingpartner.branch.timing;

import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchTimingsResponseDTO {

    private String timezone;
    private Map<String, DayScheduleDTO> weeklySchedule;
    private List<BranchClosureResponseDTO> closures;
}

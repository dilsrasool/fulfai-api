package com.fulfai.sellingpartner.branch.timing;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchClosure {

    private String id;
    private String date;
    private Boolean closedAllDay;
    private String openingTime;
    private String closingTime;
    private String reason;
}

package com.fulfai.sellingpartner.branchreview;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchReviewResponseDTO {

    private String reviewId;
    private String branchId;
    private String userId;
    private String userName;
    private Integer rating;
    private String comment;
    private Boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
}

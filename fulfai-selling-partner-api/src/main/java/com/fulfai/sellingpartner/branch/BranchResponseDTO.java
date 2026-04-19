package com.fulfai.sellingpartner.branch;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class BranchResponseDTO {

    private String companyId;
    private String branchId;
    private String name;
    private String address;
    private String city;
    private String country;
    private String phoneNumber;
    private String email;
    private String managerName;
    private Double latitude;
    private Double longitude;
    private String geoHash5;
    private String geoHash6;
    private String regularOpeningTime;
    private String regularClosingTime;
    private String dayOpeningTime;
    private String dayClosingTime;
    private String dayScheduleDate;
    private Double ratingAverage;
    private Integer ratingCount;
    private Boolean isActive;
    private Instant createdAt;
    private Instant locationUpdatedAt;
    private Instant updatedAt;
}

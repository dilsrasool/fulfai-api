package com.fulfai.sellingpartner.branch;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class Branch {

    public static final String GEOHASH5_GSI = "geoHash5-index";

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
    private String timezone;
    private Map<String, Map<String, String>> weeklySchedule;
    private List<Map<String, String>> closures;
    private String regularOpeningTime;
    private String regularClosingTime;
    private String dayOpeningTime;
    private String dayClosingTime;
    private String dayScheduleDate;
    private Double ratingAverage;
    private Integer ratingCount;
    private Long ratingSum;
    private Boolean isActive;
    private Instant createdAt;
    private Instant locationUpdatedAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("branchId")
    public String getBranchId() {
        return branchId;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("address")
    public String getAddress() {
        return address;
    }

    @DynamoDbAttribute("city")
    public String getCity() {
        return city;
    }

    @DynamoDbAttribute("country")
    public String getCountry() {
        return country;
    }

    @DynamoDbAttribute("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("managerName")
    public String getManagerName() {
        return managerName;
    }

    @DynamoDbAttribute("latitude")
    public Double getLatitude() {
        return latitude;
    }

    @DynamoDbAttribute("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @DynamoDbAttribute("geoHash5")
    public String getGeoHash5() {
        return geoHash5;
    }

    @DynamoDbAttribute("geoHash6")
    public String getGeoHash6() {
        return geoHash6;
    }

    @DynamoDbAttribute("timezone")
    public String getTimezone() {
        return timezone;
    }

    @DynamoDbAttribute("weeklySchedule")
    public Map<String, Map<String, String>> getWeeklySchedule() {
        return weeklySchedule;
    }

    @DynamoDbAttribute("closures")
    public List<Map<String, String>> getClosures() {
        return closures;
    }

    @DynamoDbAttribute("regularOpeningTime")
    public String getRegularOpeningTime() {
        return regularOpeningTime;
    }

    @DynamoDbAttribute("regularClosingTime")
    public String getRegularClosingTime() {
        return regularClosingTime;
    }

    @DynamoDbAttribute("dayOpeningTime")
    public String getDayOpeningTime() {
        return dayOpeningTime;
    }

    @DynamoDbAttribute("dayClosingTime")
    public String getDayClosingTime() {
        return dayClosingTime;
    }

    @DynamoDbAttribute("dayScheduleDate")
    public String getDayScheduleDate() {
        return dayScheduleDate;
    }

    @DynamoDbAttribute("ratingAverage")
    public Double getRatingAverage() {
        return ratingAverage;
    }

    @DynamoDbAttribute("ratingCount")
    public Integer getRatingCount() {
        return ratingCount;
    }

    @DynamoDbAttribute("ratingSum")
    public Long getRatingSum() {
        return ratingSum;
    }

    @DynamoDbAttribute("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("locationUpdatedAt")
    public Instant getLocationUpdatedAt() {
        return locationUpdatedAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

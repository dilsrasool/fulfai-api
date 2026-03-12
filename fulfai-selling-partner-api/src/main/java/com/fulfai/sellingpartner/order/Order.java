package com.fulfai.sellingpartner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
@RegisterForReflection
public class Order {

    // =========================
    // GSI CONSTANTS
    // =========================

    public static final String DATE_GSI = "date-index";

    public static final String USER_GSI = "userId-index";


    // =========================
    // PRIMARY KEY
    // =========================

    private String companyId;

    private String orderId;


    // =========================
    // NEW FIELD FOR CUSTOMER ORDERS
    // =========================

    private String userId;


    // =========================
    // EXISTING FIELDS
    // =========================

    private Instant orderDate;

    private String status;

    private String branchId;

    private List<OrderItem> items;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String paymentMethod;

    private String paymentStatus;

    private String notes;

    private Instant createdAt;

    private Instant updatedAt;


    // =========================
    // PRIMARY PARTITION KEY
    // DATE_GSI PARTITION KEY
    // =========================

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = DATE_GSI)
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }


    // =========================
    // PRIMARY SORT KEY
    // =========================

    @DynamoDbSortKey
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }


    // =========================
    // USER_GSI PARTITION KEY
    // =========================

    @DynamoDbSecondaryPartitionKey(indexNames = USER_GSI)
    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }


    // =========================
    // DATE_GSI SORT KEY
    // USER_GSI SORT KEY
    // =========================

    @DynamoDbSecondarySortKey(indexNames = {DATE_GSI, USER_GSI})
    @DynamoDbAttribute("orderDate")
    public Instant getOrderDate() {
        return orderDate;
    }


    // =========================
    // OTHER ATTRIBUTES
    // =========================

    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }


    @DynamoDbAttribute("branchId")
    public String getBranchId() {
        return branchId;
    }


    @DynamoDbAttribute("items")
    public List<OrderItem> getItems() {
        return items;
    }


    @DynamoDbAttribute("subtotal")
    public BigDecimal getSubtotal() {
        return subtotal;
    }


    @DynamoDbAttribute("taxAmount")
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }


    @DynamoDbAttribute("discountAmount")
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }


    @DynamoDbAttribute("totalAmount")
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }


    @DynamoDbAttribute("paymentMethod")
    public String getPaymentMethod() {
        return paymentMethod;
    }


    @DynamoDbAttribute("paymentStatus")
    public String getPaymentStatus() {
        return paymentStatus;
    }


    @DynamoDbAttribute("notes")
    public String getNotes() {
        return notes;
    }


    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }


    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

}

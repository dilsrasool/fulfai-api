package com.fulfai.sellingpartner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class Order {

    public static final String DATE_GSI = "date-index";

    private String companyId;
    private String orderId;
    private Instant orderDate; // UTC timestamp (GSI sort key)
    private String status; // RECEIVED, ACCEPTED, PREPARED, RIDER_ACCEPTED, SHIPPED, DELIVERED, CANCELLED
    private String branchId;
    private List<OrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod; // CASH, CARD, OTHER
    private String paymentStatus; // PENDING, PAID, REFUNDED
    private String notes;
    private Instant createdAt;  // UTC
    private Instant updatedAt;  // UTC

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = DATE_GSI)
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    @DynamoDbSecondarySortKey(indexNames = DATE_GSI)
    @DynamoDbAttribute("orderDate")
    public Instant getOrderDate() {
        return orderDate;
    }

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

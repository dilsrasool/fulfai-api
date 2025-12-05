package com.fulfai.partner.order;

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

    public static final String DATE_STATUS_GSI = "date-status-index";

    private String companyId;
    private String orderId;
    private String orderDate; // yyMMdd format
    private String status; // PENDING, ACCEPTED, PREPARED, SHIPPED, DELIVERED, CANCELLED
    private String dateStatusKey; // yyMMdd#STATUS for GSI sort key
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private String branchId;
    private List<OrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus; // PENDING, PAID, REFUNDED
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = DATE_STATUS_GSI)
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    @DynamoDbAttribute("orderDate")
    public String getOrderDate() {
        return orderDate;
    }

    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }

    @DynamoDbSecondarySortKey(indexNames = DATE_STATUS_GSI)
    @DynamoDbAttribute("dateStatusKey")
    public String getDateStatusKey() {
        return dateStatusKey;
    }

    @DynamoDbAttribute("customerName")
    public String getCustomerName() {
        return customerName;
    }

    @DynamoDbAttribute("customerPhone")
    public String getCustomerPhone() {
        return customerPhone;
    }

    @DynamoDbAttribute("customerEmail")
    public String getCustomerEmail() {
        return customerEmail;
    }

    @DynamoDbAttribute("shippingAddress")
    public String getShippingAddress() {
        return shippingAddress;
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

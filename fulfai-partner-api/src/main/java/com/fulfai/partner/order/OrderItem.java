package com.fulfai.partner.order;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
@RegisterForReflection
public class OrderItem {

    private String productId;
    private String productName;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    @DynamoDbAttribute("productId")
    public String getProductId() {
        return productId;
    }

    @DynamoDbAttribute("productName")
    public String getProductName() {
        return productName;
    }

    @DynamoDbAttribute("sku")
    public String getSku() {
        return sku;
    }

    @DynamoDbAttribute("quantity")
    public Integer getQuantity() {
        return quantity;
    }

    @DynamoDbAttribute("unitPrice")
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @DynamoDbAttribute("totalPrice")
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}

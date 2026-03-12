package com.fulfai.sellingpartner.publicapi.dto;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CreateOrderRequest {

    public String companyId;

    public String branchId;

    public List<OrderItemRequest> items;


    @Data
    @RegisterForReflection
    public static class OrderItemRequest {

        public String productId;

        public Integer quantity;

    }

}

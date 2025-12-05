package com.fulfai.partner.order;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@RegisterForReflection
public class OrderStatusUpdateDTO {

    @NotBlank(message = "Status cannot be blank")
    private String status;
}

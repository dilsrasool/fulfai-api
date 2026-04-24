package com.fulfai.sellingpartner.order;

import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class OrderActionRequestDTO {

    @NotBlank(message = "action is required")
    private String action;

    private String targetStatus;

    private String reasonCode;

    @Size(max = 500, message = "note must be <= 500 chars")
    private String note;

    @NotBlank(message = "idempotencyKey is required")
    @Size(max = 120, message = "idempotencyKey must be <= 120 chars")
    private String idempotencyKey;

    private Map<String, String> metadata;
}
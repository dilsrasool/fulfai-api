package com.fulfai.sellingpartner.account;

import java.math.BigDecimal;
import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class AccountResponseDTO {

    private String companyId;
    private String accountName;
    private Instant date;
    private BigDecimal balance;
    private BigDecimal previousBalance;
    private String lastOrderId;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.fulfai.sellingpartner.account;

import java.math.BigDecimal;
import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class Account {

    public static final String DEFAULT_ACCOUNT_NAME = "BALANCE";

    private String companyAccountKey; // companyId#accountName (partition key)
    private Instant date; // Sort key (UTC timestamp)
    private String companyId;
    private String accountName;
    private BigDecimal balance;
    private BigDecimal previousBalance;
    private String lastOrderId;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("companyAccountKey")
    public String getCompanyAccountKey() {
        return companyAccountKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("date")
    public Instant getDate() {
        return date;
    }

    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbAttribute("accountName")
    public String getAccountName() {
        return accountName;
    }

    @DynamoDbAttribute("balance")
    public BigDecimal getBalance() {
        return balance;
    }

    @DynamoDbAttribute("previousBalance")
    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    @DynamoDbAttribute("lastOrderId")
    public String getLastOrderId() {
        return lastOrderId;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static String buildCompanyAccountKey(String companyId, String accountName) {
        return companyId + "#" + (accountName != null ? accountName : DEFAULT_ACCOUNT_NAME);
    }
}

package com.fulfai.sellingpartner.product;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class ProductCsvRowErrorDTO {
    private Integer row;
    private String message;
}

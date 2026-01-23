package com.fulfai.sellingpartner.product;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ProductCsvUploadResponseDTO {

    private int total;
    private int success;
    private int failed;

    private List<ProductCsvRowErrorDTO> errors = new ArrayList<>();

    public void addError(int row, String message) {
        this.errors.add(new ProductCsvRowErrorDTO(row, message));
    }
}

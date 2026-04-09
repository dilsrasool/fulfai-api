package com.fulfai.sellingpartner.product;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductCsvService {

    @Inject
    ProductRepository productRepository;

    public ProductCsvUploadResponseDTO processCsvUpload(String companyId, String branchId, FileUpload file) {

        ProductCsvUploadResponseDTO response = new ProductCsvUploadResponseDTO();

        if (file == null) {
            throw new IllegalArgumentException("CSV file is required");
        }

        try (InputStream is = new FileInputStream(file.uploadedFile().toFile());
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IllegalArgumentException("CSV header is missing");
            }

            String[] headers = splitCsvLine(headerLine);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);

            requireHeader(headerIndex, "name");
            requireHeader(headerIndex, "category");
            requireHeader(headerIndex, "price");

            List<Product> validProducts = new ArrayList<>();

            String line;
            int rowNumber = 1;

            while ((line = br.readLine()) != null) {
                rowNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                response.setTotal(response.getTotal() + 1);

                try {
                    Product product = mapRowToProduct(companyId, branchId, headerIndex, line);
                    validProducts.add(product);
                    response.setSuccess(response.getSuccess() + 1);
                } catch (Exception e) {
                    response.setFailed(response.getFailed() + 1);
                    response.addError(rowNumber, e.getMessage());
                }
            }

            if (!validProducts.isEmpty()) {
                productRepository.batchSave(validProducts);
            }

            Log.debugf("CSV upload completed. total=%s success=%s failed=%s",
                    response.getTotal(), response.getSuccess(), response.getFailed());

            return response;

        } catch (Exception e) {
            Log.error("CSV upload failed", e);
            throw new IllegalArgumentException("Failed to process CSV: " + e.getMessage());
        }
    }

    private Product mapRowToProduct(String companyId, String branchId, Map<String, Integer> headerIndex, String line) {
        String[] cols = splitCsvLine(line);

        String name = getString(cols, headerIndex, "name");
        String category = getString(cols, headerIndex, "category");
        String priceStr = getString(cols, headerIndex, "price");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        if (priceStr == null || priceStr.isBlank()) {
            throw new IllegalArgumentException("price is required");
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("price is invalid");
        }

        Instant now = Instant.now();
        String productId = java.util.UUID.randomUUID().toString();

        Product p = new Product();
        p.setCompanyId(companyId);
        p.setBranchId(branchId);
        p.setProductId(productId);
        p.setBranchProductKey(branchId + "#" + productId);

        p.setName(name);
        p.setDescription(getString(cols, headerIndex, "description"));
        p.setCategory(category);
        p.setSku(getString(cols, headerIndex, "sku"));
        p.setBarcode(getString(cols, headerIndex, "barcode"));
        p.setUnit(getString(cols, headerIndex, "unit"));
        p.setImageUrl(getString(cols, headerIndex, "imageUrl"));

        p.setPrice(price);
        p.setCostPrice(parseBigDecimalOptional(getString(cols, headerIndex, "costPrice")));

        p.setStockQuantity(parseIntegerOptional(getString(cols, headerIndex, "stockQuantity"), 0));
        p.setReorderLevel(parseIntegerOptional(getString(cols, headerIndex, "reorderLevel"), 0));

        Boolean isActive = parseBooleanOptional(getString(cols, headerIndex, "isActive"));
        p.setIsActive(isActive != null ? isActive : true);

        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        return p;
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String key = headers[i] != null ? headers[i].trim() : "";
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private void requireHeader(Map<String, Integer> headerIndex, String key) {
        if (!headerIndex.containsKey(key)) {
            throw new IllegalArgumentException("Missing required column in CSV header: " + key);
        }
    }

    private String getString(String[] cols, Map<String, Integer> headerIndex, String key) {
        Integer idx = headerIndex.get(key);
        if (idx == null || idx < 0 || idx >= cols.length) {
            return null;
        }
        String v = cols[idx];
        if (v == null) {
            return null;
        }
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private BigDecimal parseBigDecimalOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("costPrice is invalid");
        }
    }

    private Integer parseIntegerOptional(String value, Integer defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }

    private Boolean parseBooleanOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim().toLowerCase();
        if ("true".equals(v) || "1".equals(v) || "yes".equals(v)) {
            return true;
        }
        if ("false".equals(v) || "0".equals(v) || "no".equals(v)) {
            return false;
        }
        throw new IllegalArgumentException("isActive must be true/false");
    }

    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}

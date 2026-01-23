package com.fulfai.sellingpartner.product;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import com.fulfai.sellingpartner.publicapi.dto.PublicProductDTO;


@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    @Inject
    ProductCsvService productCsvService;

    public ProductResponseDTO createProduct(String companyId, String branchId, @Valid ProductRequestDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);

        Instant now = Instant.now();
        String productId = UUID.randomUUID().toString();

        product.setCompanyId(companyId);
        product.setBranchId(branchId);
        product.setProductId(productId);
        product.setBranchProductKey(branchId + "#" + productId);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getReorderLevel() == null) {
            product.setReorderLevel(0);
        }

        productRepository.save(product);
        Log.debugf("Created product with id: %s for company: %s, branch: %s", productId, companyId, branchId);

        return productMapper.toResponseDTO(product);
    }

    public ProductResponseDTO getProductById(String companyId, String branchId, String productId) {
        Log.debugf("Getting product by companyId: %s, branchId: %s, productId: %s", companyId, branchId, productId);

        Product product = productRepository.getById(companyId, branchId, productId);
        if (product != null) {
            return productMapper.toResponseDTO(product);
        }

        throw new NotFoundException("Product not found with id: " + productId);
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByBranch(String companyId, String branchId,
            String nextToken, Integer limit) {

        Log.debugf("Getting products for company: %s, branch: %s", companyId, branchId);

        PaginatedResponse<Product> response = productRepository.getByBranch(companyId, branchId, nextToken, limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByCategory(String category,
            String nextToken, Integer limit) {

        Log.debugf("Getting products for category: %s", category);

        PaginatedResponse<Product> response = productRepository.getByCategory(category, nextToken, limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByCategoryAndCompany(String category, String companyId,
            String nextToken, Integer limit) {

        Log.debugf("Getting products for category: %s, company: %s", category, companyId);

        PaginatedResponse<Product> response = productRepository.getByCategoryAndCompany(category, companyId, nextToken,
                limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public ProductResponseDTO updateProduct(String companyId, String branchId, String productId,
            @Valid ProductRequestDTO productDTO) {

        Product originalProduct = productRepository.getById(companyId, branchId, productId);

        if (originalProduct == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        originalProduct.setName(productDTO.getName());
        originalProduct.setDescription(productDTO.getDescription());
        originalProduct.setCategory(productDTO.getCategory());
        originalProduct.setSku(productDTO.getSku());
        originalProduct.setBarcode(productDTO.getBarcode());
        originalProduct.setPrice(productDTO.getPrice());
        originalProduct.setCostPrice(productDTO.getCostPrice());
        originalProduct.setUnit(productDTO.getUnit());
        originalProduct.setStockQuantity(productDTO.getStockQuantity());
        originalProduct.setReorderLevel(productDTO.getReorderLevel());
        originalProduct.setImageUrl(productDTO.getImageUrl());
        originalProduct.setIsActive(productDTO.getIsActive());
        originalProduct.setLongitude(productDTO.getLongitude());
        originalProduct.setLatitude(productDTO.getLatitude());

        originalProduct.setCompanyId(companyId);
        originalProduct.setBranchId(branchId);
        originalProduct.setProductId(productId);
        originalProduct.setBranchProductKey(branchId + "#" + productId);
        originalProduct.setUpdatedAt(Instant.now());

        if (originalProduct.getIsActive() == null) {
            originalProduct.setIsActive(true);
        }
        if (originalProduct.getStockQuantity() == null) {
            originalProduct.setStockQuantity(0);
        }
        if (originalProduct.getReorderLevel() == null) {
            originalProduct.setReorderLevel(0);
        }

        productRepository.save(originalProduct);
        Log.debugf("Updated product with id: %s", productId);

        return productMapper.toResponseDTO(originalProduct);
    }

    public void deleteProduct(String companyId, String branchId, String productId) {
        Product product = productRepository.getById(companyId, branchId, productId);
        if (product != null) {
            productRepository.delete(companyId, branchId, productId);
            Log.debugf("Deleted product with id: %s", productId);
            return;
        }
        throw new NotFoundException("Product not found with id: " + productId);
    }

    // ✅ MULTIPART CSV upload handler
    public ProductCsvUploadResponseDTO uploadProductsFromCsv(String companyId, String branchId, FileUpload file) {
        Log.debugf("Uploading products CSV for company: %s, branch: %s", companyId, branchId);

        if (file == null) {
            throw new IllegalArgumentException("CSV file is required");
        }

        return productCsvService.processCsvUpload(companyId, branchId, file);
    }

    /* ============================
   PUBLIC BROWSING (NO AUTH)
============================ */

public PaginatedResponse<PublicProductDTO> getPublicProductsByBranch(
        String companyId,
        String branchId,
        String nextToken,
        Integer limit
) {

    if (companyId == null || companyId.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("companyId is required");
    }
    if (branchId == null || branchId.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("branchId is required");
    }

    // safety defaults
    int safeLimit = (limit == null || limit <= 0) ? 20 : Math.min(limit, 50);

    PaginatedResponse<Product> response =
            productRepository.getByBranch(companyId, branchId, nextToken, safeLimit);

    return PaginatedResponse.<PublicProductDTO>builder()
            .items(response.getItems().stream()
                    .filter(p -> p.getIsActive() == null || Boolean.TRUE.equals(p.getIsActive()))
                    .map(p -> {
                        PublicProductDTO dto = new PublicProductDTO();
                        dto.id = p.getProductId();
                        dto.branchId = p.getBranchId();
                        dto.categoryId = p.getCategory(); // your model uses "category" as String
                        dto.name = p.getName();
                        dto.description = p.getDescription();
                        dto.price = p.getPrice();
                        dto.image = p.getImageUrl();
                        dto.isAvailable = true; // optionally calculate using stockQuantity
                        return dto;
                    })
                    .collect(Collectors.toList()))
            .nextToken(response.getNextToken())
            .hasMore(response.isHasMore())
            .build();
}

/* ============================
   PUBLIC BROWSING BY CATEGORY (NO AUTH)
============================ */

public PaginatedResponse<PublicProductDTO> getPublicProductsByCategory(
        String category,
        String companyId,
        String nextToken,
        Integer limit
) {

    if (category == null || category.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("category is required");
    }

    int safeLimit = (limit == null || limit <= 0) ? 20 : Math.min(limit, 50);

    PaginatedResponse<Product> response;

    // If companyId is provided, filter category results by company
    if (companyId != null && !companyId.isBlank()) {
        response = productRepository.getByCategoryAndCompany(category, companyId, nextToken, safeLimit);
    } else {
        response = productRepository.getByCategory(category, nextToken, safeLimit);
    }

    return PaginatedResponse.<PublicProductDTO>builder()
            .items(response.getItems().stream()
                    .filter(p -> p.getIsActive() == null || Boolean.TRUE.equals(p.getIsActive()))
                    .map(p -> {
                        PublicProductDTO dto = new PublicProductDTO();
                        dto.id = p.getProductId();
                        dto.branchId = p.getBranchId();
                        dto.categoryId = p.getCategory();
                        dto.name = p.getName();
                        dto.description = p.getDescription();
                        dto.price = p.getPrice();
                        dto.image = p.getImageUrl();
                        dto.isAvailable = true;
                        return dto;
                    })
                    .collect(Collectors.toList()))
            .nextToken(response.getNextToken())
            .hasMore(response.isHasMore())
            .build();
}
public PublicProductDTO getPublicProductById(
        String companyId,
        String branchId,
        String productId
) {
    if (companyId == null || companyId.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("companyId is required");
    }
    if (branchId == null || branchId.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("branchId is required");
    }
    if (productId == null || productId.isBlank()) {
        throw new jakarta.ws.rs.BadRequestException("productId is required");
    }

    Log.debugf(
            "Public get product by id: companyId=%s, branchId=%s, productId=%s",
            companyId, branchId, productId
    );

    Product p = productRepository.getById(companyId, branchId, productId);

    if (p == null) {
        throw new NotFoundException("Product not found with id: " + productId);
    }

    // Public only returns active products
    if (p.getIsActive() != null && Boolean.FALSE.equals(p.getIsActive())) {
        throw new NotFoundException("Product not found with id: " + productId);
    }

    PublicProductDTO dto = new PublicProductDTO();
    dto.id = p.getProductId();
    //dto.c = p.getCompanyId();   // only if your PublicProductDTO has companyId field
    dto.branchId = p.getBranchId();
    dto.categoryId = p.getCategory();   // your model uses String category
    dto.name = p.getName();
    dto.description = p.getDescription();
    dto.price = p.getPrice();
    dto.image = p.getImageUrl();

    // Optional: calculate availability based on stock
    // If you want strict stock check:
     dto.isAvailable = p.getStockQuantity() != null && p.getStockQuantity() > 0;
   // dto.isAvailable = true;

    return dto;
}


}

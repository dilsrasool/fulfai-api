package com.fulfai.sellingpartner.product;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

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

        productRepository.save(product);
        Log.debugf("Created product with id: %s for company: %s, branch: %s", productId, companyId, branchId);

        return productMapper.toResponseDTO(product);
    }

    public ProductResponseDTO getProductById(String companyId, String branchId, String productId) {
        Log.debugf("Getting product by companyId: %s, branchId: %s, productId: %s", companyId, branchId, productId);
        Product product = productRepository.getById(companyId, branchId, productId);
        if (product != null) {
            return productMapper.toResponseDTO(product);
        } else {
            throw new NotFoundException("Product not found with id: " + productId);
        }
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
        PaginatedResponse<Product> response = productRepository.getByCategoryAndCompany(category, companyId, nextToken, limit);

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
        if (originalProduct != null) {
            Product product = productMapper.toEntity(productDTO);
            product.setCompanyId(companyId);
            product.setBranchId(branchId);
            product.setProductId(productId);
            product.setBranchProductKey(branchId + "#" + productId);
            product.setCreatedAt(originalProduct.getCreatedAt());
            product.setUpdatedAt(Instant.now());

            productRepository.save(product);
            Log.debugf("Updated product with id: %s", productId);

            return productMapper.toResponseDTO(product);
        } else {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }

    public void deleteProduct(String companyId, String branchId, String productId) {
        Product product = productRepository.getById(companyId, branchId, productId);
        if (product != null) {
            productRepository.delete(companyId, branchId, productId);
            Log.debugf("Deleted product with id: %s", productId);
        } else {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }
}

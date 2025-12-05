package com.fulfai.partner.product;

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

    public ProductResponseDTO createProduct(String companyId, @Valid ProductRequestDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);

        Instant now = Instant.now();
        product.setCompanyId(companyId);
        product.setProductId(UUID.randomUUID().toString());
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        productRepository.save(product);
        Log.debugf("Created product with id: %s for company: %s", product.getProductId(), companyId);

        return productMapper.toResponseDTO(product);
    }

    public ProductResponseDTO getProductById(String companyId, String productId) {
        Log.debugf("Getting product by companyId: %s, productId: %s", companyId, productId);
        Product product = productRepository.getById(companyId, productId);
        if (product != null) {
            return productMapper.toResponseDTO(product);
        } else {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByCompanyId(String companyId, String nextToken, Integer limit) {
        Log.debugf("Getting products for company: %s", companyId);
        PaginatedResponse<Product> response = productRepository.getByCompanyId(companyId, nextToken, limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByCategory(String companyId, String category,
            String nextToken, Integer limit) {
        Log.debugf("Getting products for company: %s, category: %s", companyId, category);
        PaginatedResponse<Product> response = productRepository.getByCategory(companyId, category, nextToken, limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public ProductResponseDTO updateProduct(String companyId, String productId, @Valid ProductRequestDTO productDTO) {
        Product originalProduct = productRepository.getById(companyId, productId);
        if (originalProduct != null) {
            Product product = productMapper.toEntity(productDTO);
            product.setCompanyId(companyId);
            product.setProductId(productId);
            product.setCreatedAt(originalProduct.getCreatedAt());
            product.setUpdatedAt(Instant.now());

            productRepository.save(product);
            Log.debugf("Updated product with id: %s", productId);

            return productMapper.toResponseDTO(product);
        } else {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }

    public void deleteProduct(String companyId, String productId) {
        Product product = productRepository.getById(companyId, productId);
        if (product != null) {
            productRepository.delete(companyId, productId);
            Log.debugf("Deleted product with id: %s", productId);
        } else {
            throw new NotFoundException("Product not found with id: " + productId);
        }
    }
}

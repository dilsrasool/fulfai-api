package com.fulfai.sellingpartner.product;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.publicapi.dto.PublicProductDTO;

import org.jboss.resteasy.reactive.multipart.FileUpload;

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

    @Inject
    ProductCsvService productCsvService;

    /* =========================
       CREATE
    ========================= */

    public ProductResponseDTO createProduct(
            String companyId,
            String branchId,
            @Valid ProductRequestDTO productDTO
    ) {

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

        // Image defaults
        product.setImageProcessingStatus(Product.ImageProcessingStatus.NOT_UPLOADED);

        productRepository.save(product);

        Log.debugf(
                "Created product id=%s company=%s branch=%s",
                productId, companyId, branchId
        );

        return productMapper.toResponseDTO(product);
    }

    /* =========================
       READ
    ========================= */

    public ProductResponseDTO getProductById(
            String companyId,
            String branchId,
            String productId
    ) {

        Product product = productRepository.getById(companyId, branchId, productId);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        return productMapper.toResponseDTO(product);
    }

    /* =========================
       LISTING
    ========================= */

    public PaginatedResponse<ProductResponseDTO> getProductsByBranch(
            String companyId,
            String branchId,
            String nextToken,
            Integer limit
    ) {

        PaginatedResponse<Product> response =
                productRepository.getByBranch(companyId, branchId, nextToken, limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByCategory(
            String category,
            String nextToken,
            Integer limit
    ) {

        PaginatedResponse<Product> response =
                productRepository.getByCategory(category, nextToken, limit);

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<ProductResponseDTO> getProductsByCategoryAndCompany(
            String category,
            String companyId,
            String nextToken,
            Integer limit
    ) {

        PaginatedResponse<Product> response =
                productRepository.getByCategoryAndCompany(
                        category, companyId, nextToken, limit
                );

        return PaginatedResponse.<ProductResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(productMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    /* =========================
       UPDATE
    ========================= */

    public ProductResponseDTO updateProduct(
            String companyId,
            String branchId,
            String productId,
            @Valid ProductRequestDTO productDTO
    ) {

        Product product =
                productRepository.getById(companyId, branchId, productId);

        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setCategory(productDTO.getCategory());
        product.setSku(productDTO.getSku());
        product.setBarcode(productDTO.getBarcode());
        product.setPrice(productDTO.getPrice());
        product.setCostPrice(productDTO.getCostPrice());
        product.setUnit(productDTO.getUnit());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setReorderLevel(productDTO.getReorderLevel());
        product.setIsActive(productDTO.getIsActive());
        product.setLongitude(productDTO.getLongitude());
        product.setLatitude(productDTO.getLatitude());
        product.setImageUrl(productDTO.getImageUrl());
        product.setUpdatedAt(Instant.now());

        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }

        productRepository.save(product);

        Log.debugf("Updated product id=%s", productId);

        return productMapper.toResponseDTO(product);
    }

    /* =========================
       DELETE
    ========================= */

    public void deleteProduct(
            String companyId,
            String branchId,
            String productId
    ) {

        Product product =
                productRepository.getById(companyId, branchId, productId);

        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        productRepository.delete(companyId, branchId, productId);
        Log.debugf("Deleted product id=%s", productId);
    }

    /* =========================
       IMAGE UPLOAD STATE (NEW)
    ========================= */

    public void markImageUploadStarted(
            String companyId,
            String branchId,
            String productId,
            String uploadId
    ) {

        Product product =
                productRepository.getById(companyId, branchId, productId);

        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        product.setImageProcessingStatus(Product.ImageProcessingStatus.UPLOADING);
        product.setImageUploadId(uploadId);
        product.setImageError(null);
        product.setUpdatedAt(Instant.now());

        productRepository.save(product);

        Log.debugf(
                "Image upload started product=%s uploadId=%s",
                productId, uploadId
        );
    }

    public void markImageUploadCompleted(
            String companyId,
            String branchId,
            String productId,
            String imageUrl,
            String thumbnailUrl
    ) {

        Product product =
                productRepository.getById(companyId, branchId, productId);

        if (product == null) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        Log.debug("======product updated with image uRL ====="+imageUrl);
        product.setImageUrl(imageUrl);
        product.setThumbnailUrl(thumbnailUrl);
        product.setImageProcessingStatus(Product.ImageProcessingStatus.PROCESSING);
        product.setUpdatedAt(Instant.now());

        productRepository.save(product);

        Log.debugf("Image upload finalized product=%s", productId);
    }

    /* =========================
       CSV UPLOAD
    ========================= */

    public ProductCsvUploadResponseDTO uploadProductsFromCsv(
            String companyId,
            String branchId,
            FileUpload file
    ) {

        if (file == null) {
            throw new IllegalArgumentException("CSV file is required");
        }

        return productCsvService.processCsvUpload(companyId, branchId, file);
    }

    /* =========================
       PUBLIC BROWSING (NO AUTH)
    ========================= */

    public PaginatedResponse<PublicProductDTO> getPublicProductsByBranch(
            String companyId,
            String branchId,
            String nextToken,
            Integer limit
    ) {

        int safeLimit = (limit == null || limit <= 0)
                ? 20
                : Math.min(limit, 50);

        PaginatedResponse<Product> response =
                productRepository.getByBranch(
                        companyId, branchId, nextToken, safeLimit
                );

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
                            dto.isAvailable =
                                    p.getStockQuantity() != null && p.getStockQuantity() > 0;
                            return dto;
                        })
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<PublicProductDTO> getPublicProductsByCategory(
            String category,
            String companyId,
            String nextToken,
            Integer limit
    ) {

        int safeLimit = (limit == null || limit <= 0)
                ? 20
                : Math.min(limit, 50);

        PaginatedResponse<Product> response =
                (companyId != null && !companyId.isBlank())
                        ? productRepository.getByCategoryAndCompany(
                                category, companyId, nextToken, safeLimit
                        )
                        : productRepository.getByCategory(
                                category, nextToken, safeLimit
                        );

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

        Product p =
                productRepository.getById(companyId, branchId, productId);

        if (p == null || Boolean.FALSE.equals(p.getIsActive())) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        PublicProductDTO dto = new PublicProductDTO();
        dto.id = p.getProductId();
        dto.branchId = p.getBranchId();
        dto.categoryId = p.getCategory();
        dto.name = p.getName();
        dto.description = p.getDescription();
        dto.price = p.getPrice();
        dto.image = p.getImageUrl();
        dto.isAvailable =
                p.getStockQuantity() != null && p.getStockQuantity() > 0;

        return dto;
    }
}

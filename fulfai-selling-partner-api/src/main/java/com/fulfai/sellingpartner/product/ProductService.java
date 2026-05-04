package com.fulfai.sellingpartner.product;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.location.GeoHashUtil;
import com.fulfai.sellingpartner.branch.BranchResponseDTO;
import com.fulfai.sellingpartner.branch.BranchService;
import com.fulfai.sellingpartner.publicapi.dto.PublicProductDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;



@ApplicationScoped
public class ProductService {

        private static final int DEFAULT_PUBLIC_LIMIT = 20;
        private static final int MAX_PUBLIC_LIMIT = 50;
        private static final int LOCATION_DISABLED_FETCH_MULTIPLIER = 3;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    @Inject
    ProductCsvService productCsvService;

        @Inject
        BranchService branchService;

        @ConfigProperty(name = "selling.location.search.enabled", defaultValue = "1")
        int locationSearchEnabled;

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

        int safeLimit = safePublicLimit(limit);

        PaginatedResponse<Product> response =
                productRepository.getByBranch(
                        companyId, branchId, nextToken, safeLimit
                );

        return toPublicProductResponse(response);
    }

    public PaginatedResponse<PublicProductDTO> searchPublicProductsByKeyword(
            String companyId,
            String branchId,
            String keyword,
            String nextToken,
            Integer limit
    ) {

        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("keyword is required");
        }

        int safeLimit = safePublicLimit(limit);

        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        List<Product> matchedProducts = new ArrayList<>();
        String currentNextToken = nextToken;
        boolean hasMore = false;
        int totalScanned = 0;

        boolean hasCompanyId = companyId != null && !companyId.isBlank();
        boolean hasBranchId = branchId != null && !branchId.isBlank();

        String searchScope = hasCompanyId && hasBranchId ? "branch"
                : hasCompanyId ? "company"
                : "global";

        Log.debugf(
                "PRODUCT_SEARCH_START: keyword='%s' normalizedKeyword='%s' scope=%s companyId=%s branchId=%s safeLimit=%d",
                keyword, normalizedKeyword, searchScope, companyId, branchId, safeLimit
        );

        while (matchedProducts.size() < safeLimit) {
            PaginatedResponse<Product> response;

            if (hasCompanyId && hasBranchId) {
                response = productRepository.getByBranch(
                        companyId,
                        branchId,
                        currentNextToken,
                        1
                );
            } else if (hasCompanyId) {
                response = productRepository.getByCompanyId(
                        companyId,
                        currentNextToken,
                        1
                );
            } else {
                response = productRepository.scanAll(
                        currentNextToken,
                        1
                );
            }

            if (response.getItems() == null || response.getItems().isEmpty()) {
                Log.debugf(
                        "PRODUCT_SEARCH_EXHAUSTED: totalScanned=%d matched=%d",
                        totalScanned, matchedProducts.size()
                );
                currentNextToken = null;
                hasMore = false;
                break;
            }

            Product product = response.getItems().get(0);
            totalScanned++;

            boolean branchMatch = matchesBranch(product, branchId);
            boolean activeMatch = product.getIsActive() == null || Boolean.TRUE.equals(product.getIsActive());
            boolean keywordMatch = matchesKeyword(product, normalizedKeyword);

            Log.debugf(
                    "PRODUCT_SEARCH_EVAL: productId=%s name='%s' active=%s branchMatch=%b activeMatch=%b keywordMatch=%b",
                    product.getProductId(), product.getName(), product.getIsActive(),
                    branchMatch, activeMatch, keywordMatch
            );

            if (branchMatch && activeMatch && keywordMatch) {
                matchedProducts.add(product);
                Log.debugf(
                        "PRODUCT_SEARCH_HIT: productId=%s name='%s' matchedSoFar=%d",
                        product.getProductId(), product.getName(), matchedProducts.size()
                );
            }

            currentNextToken = response.getNextToken();
            hasMore = response.isHasMore();

            if (!hasMore || currentNextToken == null) {
    Log.debugf(
    "PRODUCT_SEARCH_END: totalScanned=%d matched=%d hasMore=%b",
    (Object) totalScanned, (Object) matchedProducts.size(), (Object) hasMore
);
                break;
            }
        }

        Log.debugf(
                "PRODUCT_SEARCH_RESULT: keyword='%s' totalScanned=%d matched=%d hasMore=%b",
                keyword, totalScanned, matchedProducts.size(), hasMore
        );

        return PaginatedResponse.<PublicProductDTO>builder()
                .items(matchedProducts.stream()
                        .map(this::toPublicProductDTO)
                        .collect(Collectors.toList()))
                .nextToken(currentNextToken)
                .hasMore(hasMore)
                .build();
    }

        private boolean matchesBranch(Product product, String branchId) {
                return branchId == null || branchId.isBlank() || branchId.equals(product.getBranchId());
        }

    private boolean matchesKeyword(Product product, String normalizedKeyword) {
        String productName = product.getName() == null
                ? ""
                : product.getName().toLowerCase(Locale.ROOT);

        String productDescription = product.getDescription() == null
                ? ""
                : product.getDescription().toLowerCase(Locale.ROOT);

        return productName.contains(normalizedKeyword)
                || productDescription.contains(normalizedKeyword);
    }

    private PaginatedResponse<PublicProductDTO> toPublicProductResponse(
            PaginatedResponse<Product> response
    ) {
        return PaginatedResponse.<PublicProductDTO>builder()
                .items(response.getItems().stream()
                        .filter(p -> p.getIsActive() == null || Boolean.TRUE.equals(p.getIsActive()))
                        .map(this::toPublicProductDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public List<PublicProductDTO> getNearbyPublicProducts(
            String companyId,
            Double latitude,
            Double longitude,
            Double radiusKm,
            Integer limit
    ) {

                int safeLimit = safePublicLimit(limit);

                if (!isLocationSearchEnabled()) {
                        return getProductsWithoutLocation(companyId, safeLimit);
                }

        if (latitude == null || longitude == null) {
            throw new BadRequestException("latitude and longitude are required");
        }

        double safeRadiusKm = radiusKm == null || radiusKm <= 0 ? 10.0 : radiusKm;

        List<BranchResponseDTO> nearbyBranches =
                branchService.getNearbyBranchCandidates(companyId, latitude, longitude, safeRadiusKm);

        List<ProductDistance> nearbyProducts = new ArrayList<>();
        Set<String> seenProductIds = new HashSet<>();

        for (BranchResponseDTO branch : nearbyBranches) {
            PaginatedResponse<Product> response =
                    productRepository.getByBranch(branch.getCompanyId(), branch.getBranchId(), null, 100);

            for (Product product : response.getItems()) {
                if (Boolean.FALSE.equals(product.getIsActive())) {
                    continue;
                }

                if (!seenProductIds.add(product.getCompanyId() + "#" + product.getBranchId() + "#" + product.getProductId())) {
                    continue;
                }

                PublicProductDTO dto = toPublicProductDTO(product);
                dto.distanceKm = GeoHashUtil.calculateDistance(
                        latitude,
                        longitude,
                        branch.getLatitude(),
                        branch.getLongitude()
                );
                nearbyProducts.add(new ProductDistance(dto, dto.distanceKm));
            }
        }

        return nearbyProducts.stream()
                .sorted(Comparator.comparing(ProductDistance::distanceKm)
                        .thenComparing(pd -> pd.product().name, String.CASE_INSENSITIVE_ORDER))
                .limit(safeLimit)
                .map(ProductDistance::product)
                .collect(Collectors.toList());
    }

    private List<PublicProductDTO> getProductsWithoutLocation(String companyId, int safeLimit) {
        // Fetch all products across all pages when location search disabled
        List<Product> allProducts = new ArrayList<>();
        String nextToken = null;
        int pageSize = 100;

        do {
            PaginatedResponse<Product> response = (companyId != null && !companyId.isBlank())
                    ? productRepository.getByCompanyId(companyId, nextToken, pageSize)
                    : productRepository.scanAll(nextToken, pageSize);

            allProducts.addAll(response.getItems());
            nextToken = response.getNextToken();
        } while (nextToken != null);

        return allProducts.stream()
                .filter(p -> p.getIsActive() == null || Boolean.TRUE.equals(p.getIsActive()))
                .sorted(Comparator.comparing(
                        p -> p.getName() == null ? "" : p.getName(),
                        String.CASE_INSENSITIVE_ORDER))
                .map(this::toPublicProductDTO)
                .peek(dto -> dto.distanceKm = null)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<PublicProductDTO> getPublicProductsByCategory(
            String category,
            String companyId,
            String nextToken,
            Integer limit
    ) {

        int safeLimit = safePublicLimit(limit);

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

                return toPublicProductDTO(p);
    }

            private PublicProductDTO toPublicProductDTO(Product p) {
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
                dto.companyId = p.getCompanyId();
                return dto;
            }

            private int safePublicLimit(Integer limit) {
                return (limit == null || limit <= 0)
                        ? DEFAULT_PUBLIC_LIMIT
                        : Math.min(limit, MAX_PUBLIC_LIMIT);
            }

                        private boolean isLocationSearchEnabled() {
                                return locationSearchEnabled == 1;
                        }

            private record ProductDistance(PublicProductDTO product, Double distanceKm) {}
}

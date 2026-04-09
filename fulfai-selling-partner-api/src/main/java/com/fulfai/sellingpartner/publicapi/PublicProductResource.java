package com.fulfai.sellingpartner.publicapi;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.product.ProductService;
import com.fulfai.sellingpartner.publicapi.dto.PublicProductDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/public/products")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicProductResource {

    @Inject
    ProductService productService;

    /**
     * GET /api/selling-partner/public/products?companyId=...&branchId=...&nextToken=...&limit=...
     */
    @GET
    public PaginatedResponse<PublicProductDTO> getProducts(
            @QueryParam("companyId") String companyId,
            @QueryParam("branchId") String branchId,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit
    ) {
        return productService.getPublicProductsByBranch(companyId, branchId, nextToken, limit);
    }

        /**
         * GET /api/selling-partner/public/products/nearby?latitude=...&longitude=...&radiusKm=...&limit=...&companyId=...
         */
        @GET
        @Path("/nearby")
        public List<PublicProductDTO> getNearbyProducts(
            @QueryParam("companyId") String companyId,
            @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude,
            @QueryParam("radiusKm") @DefaultValue("10") Double radiusKm,
            @QueryParam("limit") @DefaultValue("20") Integer limit
        ) {
        return productService.getNearbyPublicProducts(
            companyId,
            latitude,
            longitude,
            radiusKm,
            limit
        );
        }

    /**
     * GET /api/selling-partner/public/products/search?companyId=...&branchId=...&keyword=...&nextToken=...&limit=...
     */
    @GET
    @Path("/search")
    public PaginatedResponse<PublicProductDTO> searchProductsByKeyword(
            @QueryParam("companyId") String companyId,
            @QueryParam("branchId") String branchId,
            @QueryParam("keyword") String keyword,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit
    ) {
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("keyword is required");
        }

        return productService.searchPublicProductsByKeyword(
                companyId,
                branchId,
                keyword,
                nextToken,
                limit
        );
    }

    /**
     * ✅ NEW:
     * GET /api/selling-partner/public/products/{productId}?companyId=...&branchId=...
     */
    @GET
    @Path("/{productId}")
    public PublicProductDTO getProductById(
            @PathParam("productId") String productId,
            @QueryParam("companyId") String companyId,
            @QueryParam("branchId") String branchId
    ) {
        PublicProductDTO dto = productService.getPublicProductById(companyId, branchId, productId);

        if (dto == null) {
            throw new NotFoundException("Product not found");
        }

        return dto;
    }
}

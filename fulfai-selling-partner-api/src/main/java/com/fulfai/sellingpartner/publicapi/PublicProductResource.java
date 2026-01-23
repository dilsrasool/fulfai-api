package com.fulfai.sellingpartner.publicapi;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.product.ProductService;
import com.fulfai.sellingpartner.publicapi.dto.PublicProductDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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

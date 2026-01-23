package com.fulfai.sellingpartner.publicapi;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.product.ProductService;
import com.fulfai.sellingpartner.publicapi.dto.PublicProductDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/public/products/by-category")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicProductCategoryResource {

    @Inject
    ProductService productService;

    @GET
    public PaginatedResponse<PublicProductDTO> getProductsByCategory(
            @QueryParam("category") String category,
            @QueryParam("companyId") String companyId,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit
    ) {
        return productService.getPublicProductsByCategory(category, companyId, nextToken, limit);
    }
}

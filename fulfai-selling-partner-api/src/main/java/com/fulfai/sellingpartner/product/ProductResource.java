package com.fulfai.sellingpartner.product;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/branch/{branchId}/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    ProductService productService;

    @POST
    public Response createProduct(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @Valid ProductRequestDTO request) {
        ProductResponseDTO createdProduct = productService.createProduct(companyId, branchId, request);
        return Response.status(Response.Status.CREATED).entity(createdProduct).build();
    }

    @POST
    @Path("/search")
    public Response searchProducts(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            PaginationDTO request) {
        Integer limit = request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        PaginatedResponse<ProductResponseDTO> products = productService.getProductsByBranch(
                companyId, branchId, request.getNextToken(), limit);
        return Response.ok(products).build();
    }

    @POST
    @Path("/search/bycategory")
    public Response searchProductsByCategory(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            ProductSearchDTO request) {
        Integer limit = request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        PaginatedResponse<ProductResponseDTO> products = productService.getProductsByCategoryAndCompany(
                request.getCategory(), companyId, request.getNextToken(), limit);
        return Response.ok(products).build();
    }

    @GET
    @Path("/{productId}")
    public Response getProductById(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId) {
        ProductResponseDTO product = productService.getProductById(companyId, branchId, productId);
        return Response.ok(product).build();
    }

    @PUT
    @Path("/{productId}")
    public Response updateProduct(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId,
            @Valid ProductRequestDTO request) {
        ProductResponseDTO product = productService.updateProduct(companyId, branchId, productId, request);
        return Response.ok(product).build();
    }

    @DELETE
    @Path("/{productId}")
    public Response deleteProduct(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId) {
        productService.deleteProduct(companyId, branchId, productId);
        return Response.noContent().build();
    }
}

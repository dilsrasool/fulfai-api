package com.fulfai.sellingpartner.product;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Authenticated
@Path("/company/{companyId}/branch/{branchId}/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    ProductService productService;

    @Inject
    ProductImageUploadService productImageUploadService;

    /* =========================
       PRODUCT CRUD
    ========================= */

    @POST
    public Response createProduct(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @Valid ProductRequestDTO request
    ) {
        ProductResponseDTO created =
                productService.createProduct(companyId, branchId, request);

        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @POST
    @Path("/search")
    public Response searchProducts(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            PaginationDTO request
    ) {
        Integer limit =
                request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;

        PaginatedResponse<ProductResponseDTO> products =
                productService.getProductsByBranch(
                        companyId,
                        branchId,
                        request.getNextToken(),
                        limit
                );

        return Response.ok(products).build();
    }

    @POST
    @Path("/search/bycategory")
    public Response searchProductsByCategory(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            ProductSearchDTO request
    ) {
        Integer limit =
                request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;

        PaginatedResponse<ProductResponseDTO> products =
                productService.getProductsByCategoryAndCompany(
                        request.getCategory(),
                        companyId,
                        request.getNextToken(),
                        limit
                );

        return Response.ok(products).build();
    }

    @GET
    @Path("/{productId}")
    public Response getProductById(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId
    ) {
        ProductResponseDTO product =
                productService.getProductById(companyId, branchId, productId);

        return Response.ok(product).build();
    }

    @PUT
    @Path("/{productId}")
    public Response updateProduct(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId,
            @Valid ProductRequestDTO request
    ) {
        ProductResponseDTO updated =
                productService.updateProduct(companyId, branchId, productId, request);

        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{productId}")
    public Response deleteProduct(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId
    ) {
        productService.deleteProduct(companyId, branchId, productId);
        return Response.noContent().build();
    }

    /* =========================
       IMAGE UPLOAD (PRESIGNED ONLY)
       Flow:
       1) frontend asks for upload-url
       2) frontend PUTs directly to S3
       3) frontend calls finalize
    ========================= */

    @POST
    @Path("/{productId}/image/upload-url")
    public Response generateImageUploadUrl(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("productId") String productId,
            ProductImageUploadService.UploadRequest request
    ) {
        request.productId = productId;

        ProductImageUploadService.PresignedUploadResponse response =
                productImageUploadService.generatePresignedUploadUrl(
                        companyId,
                        branchId,
                        request
                );

        return Response.ok(response).build();
    }

    @POST
@Path("/{productId}/image/finalize")
public Response finalizeImageUpload(
        @PathParam("companyId") String companyId,
        @PathParam("branchId") String branchId,
        @PathParam("productId") String productId,
        ImageFinalizeRequest request
) {
    // confirm upload + update DB
    String imageUrl = productImageUploadService.confirmUpload(
            companyId,
            branchId,
            productId,
            request.uploadId,
            request.originalKey
    );

    // ✅ MUST return JSON for frontend
    return Response.ok(
            java.util.Map.of("imageUrl", imageUrl)
    ).build();
}


    /* =========================
       CSV UPLOAD
    ========================= */

    @POST
    @Path("/upload-csv")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCsv(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            org.jboss.resteasy.reactive.multipart.FileUpload file
    ) {
        ProductCsvUploadResponseDTO result =
                productService.uploadProductsFromCsv(companyId, branchId, file);

        return Response.ok(result).build();
    }

    /* =========================
       DTOs
    ========================= */

    public static class ImageFinalizeRequest {
        public String uploadId;
        public String originalKey;
    }
}

package com.fulfai.sellingpartner.category;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/categories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    // --------------------------------------------------
    // Create category (company-scoped)
    // --------------------------------------------------
    @POST
    public Response createCategory(
            @PathParam("companyId") @NotBlank String companyId,
            @NotNull @Valid CategoryRequestDTO request
    ) {
        return Response
                .status(Response.Status.CREATED)
                .entity(categoryService.createCategory(companyId, request))
                .build();
    }

    // --------------------------------------------------
    // Get all categories (company-scoped)
    // --------------------------------------------------
    @GET
    public Response getAllCategories(
            @PathParam("companyId") @NotBlank String companyId
    ) {
        List<CategoryResponseDTO> categories =
                categoryService.getAllCategories(companyId);

        return Response.ok(categories).build();
    }

    // --------------------------------------------------
    // Get category by ID (company-scoped)
    // --------------------------------------------------
    @GET
    @Path("/{categoryId}")
    public Response getCategoryById(
            @PathParam("companyId") @NotBlank String companyId,
            @PathParam("categoryId") @NotBlank String categoryId
    ) {
        return Response.ok(
                categoryService.getCategoryById(companyId, categoryId)
        ).build();
    }

    // --------------------------------------------------
    // Update category (company-scoped)
    // --------------------------------------------------
    @PUT
    @Path("/{categoryId}")
    public Response updateCategory(
            @PathParam("companyId") @NotBlank String companyId,
            @PathParam("categoryId") @NotBlank String categoryId,
            @NotNull @Valid CategoryRequestDTO request
    ) {
        return Response.ok(
                categoryService.updateCategory(companyId, categoryId, request)
        ).build();
    }

    // --------------------------------------------------
    // Delete category (company-scoped)
    // --------------------------------------------------
    @DELETE
    @Path("/{categoryId}")
    public Response deleteCategory(
            @PathParam("companyId") @NotBlank String companyId,
            @PathParam("categoryId") @NotBlank String categoryId
    ) {
        categoryService.deleteCategory(companyId, categoryId);
        return Response.noContent().build();
    }
}

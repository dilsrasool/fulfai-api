package com.fulfai.partner.category;

import com.fulfai.common.dto.PaginatedResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @POST
    public Response createCategory(@PathParam("companyId") String companyId, @Valid CategoryRequestDTO request) {
        CategoryResponseDTO createdCategory = categoryService.createCategory(companyId, request);
        return Response.status(Response.Status.CREATED).entity(createdCategory).build();
    }

    @GET
    public Response getCategories(@PathParam("companyId") String companyId,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit) {
        PaginatedResponse<CategoryResponseDTO> categories = categoryService.getCategoriesByCompanyId(companyId, nextToken, limit);
        return Response.ok(categories).build();
    }

    @GET
    @Path("/{name}")
    public Response getCategoryByName(@PathParam("companyId") String companyId,
            @PathParam("name") String name) {
        CategoryResponseDTO category = categoryService.getCategoryByName(companyId, name);
        return Response.ok(category).build();
    }

    @PUT
    @Path("/{name}")
    public Response updateCategory(@PathParam("companyId") String companyId,
            @PathParam("name") String name,
            @Valid CategoryRequestDTO request) {
        CategoryResponseDTO category = categoryService.updateCategory(companyId, name, request);
        return Response.ok(category).build();
    }

    @DELETE
    @Path("/{name}")
    public Response deleteCategory(@PathParam("companyId") String companyId,
            @PathParam("name") String name) {
        categoryService.deleteCategory(companyId, name);
        return Response.noContent().build();
    }
}

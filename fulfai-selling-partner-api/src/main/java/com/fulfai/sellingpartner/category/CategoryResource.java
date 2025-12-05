package com.fulfai.sellingpartner.category;

import java.util.List;

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

@Path("/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @POST
    public Response createCategory(@Valid CategoryRequestDTO request) {
        CategoryResponseDTO createdCategory = categoryService.createCategory(request);
        return Response.status(Response.Status.CREATED).entity(createdCategory).build();
    }

    @GET
    public Response getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return Response.ok(categories).build();
    }

    @GET
    @Path("/{name}")
    public Response getCategoryByName(@PathParam("name") String name) {
        CategoryResponseDTO category = categoryService.getCategoryByName(name);
        return Response.ok(category).build();
    }

    @PUT
    @Path("/{name}")
    public Response updateCategory(@PathParam("name") String name, @Valid CategoryRequestDTO request) {
        CategoryResponseDTO category = categoryService.updateCategory(name, request);
        return Response.ok(category).build();
    }

    @DELETE
    @Path("/{name}")
    public Response deleteCategory(@PathParam("name") String name) {
        categoryService.deleteCategory(name);
        return Response.noContent().build();
    }
}

package com.fulfai.sellingpartner.publicapi;

import java.util.List;

import com.fulfai.sellingpartner.category.CategoryService;
import com.fulfai.sellingpartner.publicapi.dto.PublicCategoryDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/public/categories")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicCategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    public List<PublicCategoryDTO> getCategories(@QueryParam("companyId") String companyId) {
        return categoryService.getPublicCategories(companyId);
    }
}



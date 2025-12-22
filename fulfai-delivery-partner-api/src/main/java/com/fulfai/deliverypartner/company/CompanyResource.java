package com.fulfai.deliverypartner.company;

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

@Path("/company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {

    @Inject
    CompanyService companyService;

    @POST
    public Response createCompany(@Valid CompanyRequestDTO companyDTO) {
        CompanyResponseDTO company = companyService.createCompany(companyDTO);
        return Response.status(Response.Status.CREATED).entity(company).build();
    }

    @GET
    @Path("/{id}")
    public Response getCompany(@PathParam("id") String id) {
        CompanyResponseDTO company = companyService.getCompanyById(id);
        return Response.ok(company).build();
    }

    

    @PUT
    @Path("/{id}")
    public Response updateCompany(@PathParam("id") String id, @Valid CompanyRequestDTO companyDTO) {
        CompanyResponseDTO company = companyService.updateCompany(id, companyDTO);
        return Response.ok(company).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCompany(@PathParam("id") String id) {
        companyService.deleteCompany(id);
        return Response.noContent().build();
    }
}

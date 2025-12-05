package com.fulfai.partner.company;

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
    public Response createCompany(@Valid CompanyRequestDTO request) {
        CompanyResponseDTO createdCompany = companyService.createCompany(request);
        return Response.status(Response.Status.CREATED).entity(createdCompany).build();
    }

    @GET
    @Path("/{id}")
    public Response getCompanyById(@PathParam("id") String id) {
        CompanyResponseDTO company = companyService.getCompanyById(id);
        return Response.ok(company).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCompanyById(@PathParam("id") String id, @Valid CompanyRequestDTO request) {
        CompanyResponseDTO company = companyService.updateCompanyById(id, request);
        return Response.ok(company).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCompanyById(@PathParam("id") String id) {
        companyService.deleteCompanyById(id);
        return Response.noContent().build();
    }
}

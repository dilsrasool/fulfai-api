package com.fulfai.sellingpartner.company;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import io.quarkus.security.Authenticated;

@Path("/company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated   // ✅ secure all endpoints
public class CompanyResource {

    @Inject
    CompanyService companyService;

    /* =========================
       CREATE COMPANY
    ========================== */

    @POST
    public Response createCompany(@Valid CompanyRequestDTO request) {
        CompanyResponseDTO createdCompany =
                companyService.createCompany(request);

        return Response
                .status(Response.Status.CREATED)
                .entity(createdCompany)
                .build();
    }

    /* =========================
       GET COMPANY BY ID
    ========================== */

    @GET
    @Path("/{id}")
    public Response getCompanyById(@PathParam("id") String id) {
        CompanyResponseDTO company =
                companyService.getCompanyById(id);

        return Response.ok(company).build();
    }

    /* =========================
       GET DEFAULT COMPANY
       (LEGACY / PRIMARY)
    ========================== */

    @GET
    @Path("/me")
    public CompanyResponseDTO getMyCompany() {
        return companyService.getCompanyForCurrentUser();
    }

    /* =========================
       GET ALL COMPANIES
       FOR CURRENT USER
    ========================== */

    @GET
    @Path("/my-companies")
    public List<CompanyResponseDTO> getAllMyCompanies() {
        return companyService.getAllCompaniesForCurrentUser();
    }

    /* =========================
       UPDATE COMPANY
    ========================== */

    @PUT
    @Path("/{id}")
    public Response updateCompanyById(
            @PathParam("id") String id,
            @Valid CompanyRequestDTO request
    ) {
        CompanyResponseDTO updatedCompany =
                companyService.updateCompanyById(id, request);

        return Response.ok(updatedCompany).build();
    }

    /* =========================
       DELETE COMPANY
    ========================== */

    @DELETE
    @Path("/{id}")
    public Response deleteCompanyById(@PathParam("id") String id) {
        companyService.deleteCompanyById(id);
        return Response.noContent().build();
    }
}

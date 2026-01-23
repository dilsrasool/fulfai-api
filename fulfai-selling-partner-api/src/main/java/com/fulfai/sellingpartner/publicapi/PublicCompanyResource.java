package com.fulfai.sellingpartner.publicapi;

import java.util.List;

import com.fulfai.sellingpartner.company.CompanyService;
import com.fulfai.sellingpartner.publicapi.dto.PublicCompanyDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/public/companies")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicCompanyResource {

    @Inject
    CompanyService companyService;

    @GET
    public List<PublicCompanyDTO> getCompanies() {
        return companyService.getPublicCompanies();
    }

    // ✅ NEW: GET /api/selling-partner/public/companies/{companyId}
    @GET
    @Path("/{companyId}")
    public PublicCompanyDTO getCompanyById(@PathParam("companyId") String companyId) {
        return companyService.getPublicCompanyById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));
    }
}

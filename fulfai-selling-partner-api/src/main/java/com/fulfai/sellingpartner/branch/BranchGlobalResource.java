package com.fulfai.sellingpartner.branch;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/branch")
@Produces(MediaType.APPLICATION_JSON)
public class BranchGlobalResource {

    @Inject
    BranchService branchService;

    @GET
    @Path("/active")
    @PermitAll
    public Response getAllActiveBranchesAcrossAllCompanies() {
        return Response.ok(branchService.getAllActiveBranchesAcrossAllCompanies()).build();
    }
}

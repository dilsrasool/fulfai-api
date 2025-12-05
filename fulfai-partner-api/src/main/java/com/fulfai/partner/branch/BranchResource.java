package com.fulfai.partner.branch;

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

@Path("/company/{companyId}/branch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BranchResource {

    @Inject
    BranchService branchService;

    @POST
    public Response createBranch(@PathParam("companyId") String companyId, @Valid BranchRequestDTO request) {
        BranchResponseDTO createdBranch = branchService.createBranch(companyId, request);
        return Response.status(Response.Status.CREATED).entity(createdBranch).build();
    }

    @GET
    public Response getBranches(@PathParam("companyId") String companyId,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit) {
        PaginatedResponse<BranchResponseDTO> branches = branchService.getBranchesByCompanyId(companyId, nextToken, limit);
        return Response.ok(branches).build();
    }

    @GET
    @Path("/{branchId}")
    public Response getBranchById(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId) {
        BranchResponseDTO branch = branchService.getBranchById(companyId, branchId);
        return Response.ok(branch).build();
    }

    @PUT
    @Path("/{branchId}")
    public Response updateBranch(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @Valid BranchRequestDTO request) {
        BranchResponseDTO branch = branchService.updateBranch(companyId, branchId, request);
        return Response.ok(branch).build();
    }

    @DELETE
    @Path("/{branchId}")
    public Response deleteBranch(@PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId) {
        branchService.deleteBranch(companyId, branchId);
        return Response.noContent().build();
    }
}

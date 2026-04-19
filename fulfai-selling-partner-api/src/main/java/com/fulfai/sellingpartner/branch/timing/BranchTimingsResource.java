package com.fulfai.sellingpartner.branch.timing;

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

@Path("/company/{companyId}/branch/{branchId}/timings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BranchTimingsResource {

    @Inject
    BranchTimingsService branchTimingsService;

    @GET
    public Response getTimings(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId
    ) {
        return Response.ok(branchTimingsService.getTimings(companyId, branchId)).build();
    }

    @PUT
    public Response updateTimings(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @Valid BranchTimingsUpdateRequestDTO request
    ) {
        return Response.ok(branchTimingsService.updateTimings(companyId, branchId, request)).build();
    }

    @POST
    @Path("/closures")
    public Response addClosure(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @Valid BranchClosureRequestDTO request
    ) {
        BranchClosureResponseDTO created = branchTimingsService.addClosure(companyId, branchId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/closures/{closureId}")
    public Response updateClosure(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("closureId") String closureId,
            @Valid BranchClosureRequestDTO request
    ) {
        return Response.ok(branchTimingsService.updateClosure(companyId, branchId, closureId, request)).build();
    }

    @DELETE
    @Path("/closures/{closureId}")
    public Response deleteClosure(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("closureId") String closureId
    ) {
        branchTimingsService.deleteClosure(companyId, branchId, closureId);
        return Response.noContent().build();
    }
}

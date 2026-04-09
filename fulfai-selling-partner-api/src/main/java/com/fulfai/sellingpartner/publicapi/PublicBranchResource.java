package com.fulfai.sellingpartner.publicapi;

import java.util.List;

import com.fulfai.sellingpartner.branch.BranchService;
import com.fulfai.sellingpartner.publicapi.dto.PublicBranchDTO;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/public/branches")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicBranchResource {

    @Inject
    BranchService branchService;

    @GET
    public List<PublicBranchDTO> getBranches(@QueryParam("companyId") String companyId) {
        return branchService.getPublicBranches(companyId);
    }

    @GET
    @Path("/nearby")
    public List<PublicBranchDTO> getNearbyBranches(
            @QueryParam("companyId") String companyId,
            @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude,
            @QueryParam("radiusKm") @DefaultValue("10") Double radiusKm,
            @QueryParam("limit") @DefaultValue("20") Integer limit
    ) {
        return branchService.getNearbyPublicBranches(
                companyId,
                latitude,
                longitude,
                radiusKm,
                limit
        );
    }
}

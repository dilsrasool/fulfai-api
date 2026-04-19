package com.fulfai.sellingpartner.publicapi;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.security.CognitoSecurityContext;
import com.fulfai.sellingpartner.branchreview.BranchReviewRequestDTO;
import com.fulfai.sellingpartner.branchreview.BranchReviewResponseDTO;
import com.fulfai.sellingpartner.branchreview.BranchReviewService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/public/branches/{branchId}/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicBranchReviewResource {

    @Inject
    BranchReviewService branchReviewService;

    @Inject
    CognitoSecurityContext securityContext;

    @GET
    @PermitAll
    public PaginatedResponse<BranchReviewResponseDTO> getReviews(
            @PathParam("branchId") String branchId,
            @QueryParam("companyId") String companyId,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit
    ) {
        return branchReviewService.getReviews(companyId, branchId, nextToken, limit);
    }

    @POST
    @RolesAllowed("customer")
    public BranchReviewResponseDTO createOrReplaceReview(
            @PathParam("branchId") String branchId,
            @QueryParam("companyId") String companyId,
            @Valid BranchReviewRequestDTO request
    ) {
        return branchReviewService.upsertReview(companyId, branchId, requireUserId(), request);
    }

    @PUT
    @RolesAllowed("customer")
    public BranchReviewResponseDTO updateReview(
            @PathParam("branchId") String branchId,
            @QueryParam("companyId") String companyId,
            @Valid BranchReviewRequestDTO request
    ) {
        return branchReviewService.upsertReview(companyId, branchId, requireUserId(), request);
    }

    @DELETE
    @RolesAllowed("customer")
    public void deleteOwnReview(
            @PathParam("branchId") String branchId,
            @QueryParam("companyId") String companyId
    ) {
        branchReviewService.deleteOwnReview(companyId, branchId, requireUserId());
    }

    private String requireUserId() {
        String userId = securityContext.getUserSub();
        if (userId == null || userId.isBlank()) {
            throw new jakarta.ws.rs.NotAuthorizedException("Login required");
        }
        return userId;
    }
}

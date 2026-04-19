package com.fulfai.sellingpartner.branchreview;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.security.CognitoSecurityContext;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.validation.Valid;

@Path("/company/{companyId}/branch/{branchId}/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BranchReviewResource {

    @Inject
    BranchReviewService branchReviewService;

        @Inject
        CognitoSecurityContext securityContext;

        @POST
        public Response createReview(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @Valid BranchReviewRequestDTO request
        ) {
        BranchReviewResponseDTO response = branchReviewService.createReview(
            companyId,
            branchId,
            requireUserId(),
            request
        );
        return Response.status(Response.Status.CREATED).entity(response).build();
        }

    @GET
    public Response getReviews(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit
    ) {
        PaginatedResponse<BranchReviewResponseDTO> response = branchReviewService.getReviews(
                companyId,
                branchId,
                nextToken,
                limit
        );
        return Response.ok(response).build();
    }

    @GET
    @Path("/{reviewId}")
    public Response getReviewById(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("reviewId") String reviewId
    ) {
        BranchReviewResponseDTO response = branchReviewService.getReviewById(companyId, branchId, reviewId);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{reviewId}")
    public Response updateReview(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("reviewId") String reviewId,
            @Valid BranchReviewRequestDTO request
    ) {
        BranchReviewResponseDTO response = branchReviewService.updateReviewById(companyId, branchId, reviewId, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{reviewId}")
    public Response deleteReview(
            @PathParam("companyId") String companyId,
            @PathParam("branchId") String branchId,
            @PathParam("reviewId") String reviewId
    ) {
        branchReviewService.deleteByReviewId(companyId, branchId, reviewId);
        return Response.noContent().build();
    }

    private String requireUserId() {
        String userId = securityContext.getUserSub();
        if (userId == null || userId.isBlank()) {
            throw new NotAuthorizedException("Login required");
        }
        return userId;
    }
}

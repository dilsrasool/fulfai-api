package com.fulfai.sellingpartner.companyJoinRequest;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;
import com.fulfai.sellingpartner.security.ApprovalTokenUtil;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/join-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated // default: all endpoints require auth unless overridden
public class CompanyJoinRequestResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    CompanyJoinRequestService companyJoinRequestService;

    /* =========================
       LIST JOIN REQUESTS
       (OWNER VIEW)
    ========================== */

    @POST
    @Path("/list")
    public Response listJoinRequests(
            @PathParam("companyId") String companyId,
            @QueryParam("status") String status,
            PaginationDTO pagination
    ) {

        Integer limit =
                pagination != null && pagination.getLimit() != null
                        ? pagination.getLimit()
                        : DEFAULT_LIMIT;

        String nextToken =
                pagination != null ? pagination.getNextToken() : null;

        PaginatedResponse<CompanyJoinRequestResponseDTO> response =
                companyJoinRequestService.listJoinRequests(
                        companyId,
                        status,
                        nextToken,
                        limit
                );

        return Response.ok(response).build();
    }

    /* =========================
       REQUEST TO JOIN COMPANY
       (AUTH USER)
    ========================== */

    @POST
    @Path("/request")
    public Response requestToJoinCompany(
            @PathParam("companyId") String companyId,
            CompanyJoinRequestCreateDTO request
    ) {

        CompanyJoinRequestResponseDTO response =
                companyJoinRequestService.createJoinRequest(
                        companyId,
                        request
                );

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    /* =========================
       APPROVE JOIN REQUEST
       (OWNER – UI BUTTON)
    ========================== */

    @POST
    @Path("/{requestId}/approve")
    public Response approveJoinRequest(
            @PathParam("companyId") String companyId,
            @PathParam("requestId") String requestId
    ) {

        companyJoinRequestService.approveJoinRequest(
                companyId,
                requestId
        );

        return Response.noContent().build();
    }

    /* =========================
       REJECT JOIN REQUEST
       (OWNER – UI BUTTON)
    ========================== */

    @POST
    @Path("/{requestId}/reject")
    public Response rejectJoinRequest(
            @PathParam("companyId") String companyId,
            @PathParam("requestId") String requestId
    ) {

        companyJoinRequestService.rejectJoinRequest(
                companyId,
                requestId
        );

        return Response.noContent().build();
    }

    /* =========================
       APPROVE JOIN REQUEST
       (EMAIL LINK – PUBLIC)
    ========================== */

    @POST
    @Path("/approve-by-token")
    @PermitAll // overrides @Authenticated
    public Response approveByToken(
            @PathParam("companyId") String companyId,
            @QueryParam("token") String token
    ) {

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Approval token is missing");
        }

        ApprovalTokenUtil.TokenData data =
                ApprovalTokenUtil.validateToken(token);

        // Safety check: token must match URL company
        if (!companyId.equals(data.getCompanyId())) {
            throw new BadRequestException("Token does not match company");
        }

        companyJoinRequestService.approveJoinRequestByToken(
                data.getCompanyId(),
                data.getRequestId()
        );

        return Response.ok().build();
    }

    /* =========================
       REJECT JOIN REQUEST
       (EMAIL LINK – PUBLIC)
    ========================== */

    @POST
    @Path("/reject-by-token")
    @PermitAll
    public Response rejectByToken(
            @PathParam("companyId") String companyId,
            @QueryParam("token") String token
    ) {

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Rejection token is missing");
        }

        ApprovalTokenUtil.TokenData data =
                ApprovalTokenUtil.validateToken(token);

        if (!companyId.equals(data.getCompanyId())) {
            throw new BadRequestException("Token does not match company");
        }

        companyJoinRequestService.rejectJoinRequestByToken(
                data.getCompanyId(),
                data.getRequestId()
        );

        return Response.ok().build();
    }
}

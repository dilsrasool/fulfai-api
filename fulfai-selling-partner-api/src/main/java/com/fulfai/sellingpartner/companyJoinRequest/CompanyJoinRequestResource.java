package com.fulfai.sellingpartner.companyJoinRequest;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

import jakarta.inject.Inject;
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
public class CompanyJoinRequestResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    CompanyJoinRequestService companyJoinRequestService;

    /* =========================
       LIST JOIN REQUESTS
       (Admin view)
    ========================== */

    @POST
    @Path("/list")
    public Response listJoinRequests(
            @PathParam("companyId") String companyId,
            @QueryParam("status") String status,
            PaginationDTO request
    ) {

        Integer limit =
                request != null && request.getLimit() != null
                        ? request.getLimit()
                        : DEFAULT_LIMIT;

        String nextToken =
                request != null ? request.getNextToken() : null;

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
       (User action)
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
       (Admin action)
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

        return Response.ok().build();
    }

    /* =========================
       REJECT JOIN REQUEST
       (Admin action)
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

        return Response.ok().build();
    }
}

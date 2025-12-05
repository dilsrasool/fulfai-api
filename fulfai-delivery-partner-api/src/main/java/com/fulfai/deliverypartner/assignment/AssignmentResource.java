package com.fulfai.deliverypartner.assignment;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/driver/{driverId}/assignment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssignmentResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    AssignmentService assignmentService;

    @POST
    public Response assignOrder(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            @Valid AssignmentRequestDTO assignmentDTO) {
        AssignmentResponseDTO assignment = assignmentService.assignOrder(companyId, driverId, assignmentDTO);
        return Response.status(Response.Status.CREATED).entity(assignment).build();
    }

    @POST
    @Path("/search")
    public Response getDriverAssignments(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            PaginationDTO request) {
        Integer limit = request != null && request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        String nextToken = request != null ? request.getNextToken() : null;
        PaginatedResponse<AssignmentResponseDTO> assignments = assignmentService.getDriverAssignments(driverId,
                nextToken, limit);
        return Response.ok(assignments).build();
    }

    @PUT
    @Path("/order/{orderId}/status")
    public Response updateAssignmentStatus(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            @PathParam("orderId") String orderId,
            @QueryParam("status") String status) {
        AssignmentResponseDTO assignment = assignmentService.updateAssignmentStatus(driverId, orderId, status);
        return Response.ok(assignment).build();
    }
}

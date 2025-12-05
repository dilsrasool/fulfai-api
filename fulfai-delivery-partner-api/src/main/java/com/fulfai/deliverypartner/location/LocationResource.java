package com.fulfai.deliverypartner.location;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/driver/{driverId}/location")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocationResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    LocationService locationService;

    @POST
    public Response updateLocation(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            @Valid LocationUpdateDTO locationDTO) {
        LocationResponseDTO location = locationService.updateLocation(companyId, driverId, locationDTO);
        return Response.status(Response.Status.CREATED).entity(location).build();
    }

    @POST
    @Path("/history")
    public Response getLocationHistory(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            PaginationDTO request) {
        Integer limit = request != null && request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        String nextToken = request != null ? request.getNextToken() : null;
        PaginatedResponse<LocationResponseDTO> history = locationService.getDriverLocationHistory(driverId, nextToken,
                limit);
        return Response.ok(history).build();
    }
}

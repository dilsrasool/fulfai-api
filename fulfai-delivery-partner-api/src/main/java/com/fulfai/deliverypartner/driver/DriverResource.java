package com.fulfai.deliverypartner.driver;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dto.PaginationDTO;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/driver")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DriverResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    DriverService driverService;

    @POST
    public Response createDriver(@PathParam("companyId") String companyId,
            @Valid DriverRequestDTO driverDTO) {
        DriverResponseDTO driver = driverService.createDriver(companyId, driverDTO);
        return Response.status(Response.Status.CREATED).entity(driver).build();
    }

    @GET
    @Path("/{driverId}")
    public Response getDriver(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId) {
        DriverResponseDTO driver = driverService.getDriverById(companyId, driverId);
        return Response.ok(driver).build();
    }

    @POST
    @Path("/search")
    public Response getDriversByCompany(@PathParam("companyId") String companyId,
            PaginationDTO request) {
        Integer limit = request != null && request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        String nextToken = request != null ? request.getNextToken() : null;
        PaginatedResponse<DriverResponseDTO> drivers = driverService.getDriversByCompany(companyId, nextToken, limit);
        return Response.ok(drivers).build();
    }

    @PUT
    @Path("/{driverId}")
    public Response updateDriver(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            @Valid DriverRequestDTO driverDTO) {
        DriverResponseDTO driver = driverService.updateDriver(companyId, driverId, driverDTO);
        return Response.ok(driver).build();
    }

    @PUT
    @Path("/{driverId}/status")
    public Response updateDriverStatus(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId,
            @QueryParam("status") String status) {
        DriverResponseDTO driver = driverService.updateDriverStatus(companyId, driverId, status);
        return Response.ok(driver).build();
    }

    @DELETE
    @Path("/{driverId}")
    public Response deleteDriver(@PathParam("companyId") String companyId,
            @PathParam("driverId") String driverId) {
        driverService.deleteDriver(companyId, driverId);
        return Response.noContent().build();
    }
}

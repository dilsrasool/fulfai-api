package com.fulfai.sellingpartner.order;

import com.fulfai.common.dto.PaginatedResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/order")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    OrderService orderService;


    // =========================
    // CREATE ORDER (SELLER)
    // =========================

    @POST
    public Response createOrder(
            @PathParam("companyId") String companyId,
            @Valid OrderRequestDTO request
    ) {

        OrderResponseDTO createdOrder =
                orderService.createOrder(companyId, request);

        return Response
                .status(Response.Status.CREATED)
                .entity(createdOrder)
                .build();
    }


    // =========================
    // SEARCH BY DATE
    // =========================

    @POST
    @Path("/search/bydate")
    public Response searchOrdersByDate(
            @PathParam("companyId") String companyId,
            @Valid OrderSearchDTO request
    ) {

        Integer limit =
                request.getLimit() != null
                        ? request.getLimit()
                        : DEFAULT_LIMIT;

        PaginatedResponse<OrderResponseDTO> orders =
                orderService.getOrdersByDateRange(
                        companyId,
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getNextToken(),
                        limit
                );

        return Response.ok(orders).build();
    }


    // =========================
    // GET BY ID
    // =========================

    @GET
    @Path("/{orderId}")
    public Response getOrderById(
            @PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId
    ) {

        OrderResponseDTO order =
                orderService.getOrderById(companyId, orderId);

        return Response.ok(order).build();
    }


    // =========================
    // UPDATE
    // =========================

    @PUT
    @Path("/{orderId}")
    public Response updateOrder(
            @PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId,
            @Valid OrderRequestDTO request
    ) {

        OrderResponseDTO order =
                orderService.updateOrder(companyId, orderId, request);

        return Response.ok(order).build();
    }


    // =========================
    // DELETE
    // =========================

    @DELETE
    @Path("/{orderId}")
    public Response deleteOrder(
            @PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId
    ) {

        orderService.deleteOrder(companyId, orderId);

        return Response.noContent().build();
    }

}

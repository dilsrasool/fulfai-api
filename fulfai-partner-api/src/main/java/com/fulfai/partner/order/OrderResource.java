package com.fulfai.partner.order;

import com.fulfai.common.dto.PaginatedResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/order")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    OrderService orderService;

    @POST
    public Response createOrder(@PathParam("companyId") String companyId, @Valid OrderRequestDTO request) {
        OrderResponseDTO createdOrder = orderService.createOrder(companyId, request);
        return Response.status(Response.Status.CREATED).entity(createdOrder).build();
    }

    @POST
    @Path("/search/bydate")
    public Response searchOrdersByDate(@PathParam("companyId") String companyId, @Valid OrderSearchDTO request) {
        Integer limit = request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT;
        PaginatedResponse<OrderResponseDTO> orders = orderService.getOrdersByDateRange(
                companyId, request.getStartDate(), request.getEndDate(), request.getNextToken(), limit);
        return Response.ok(orders).build();
    }

    @GET
    @Path("/{orderId}")
    public Response getOrderById(@PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId) {
        OrderResponseDTO order = orderService.getOrderById(companyId, orderId);
        return Response.ok(order).build();
    }

    @PUT
    @Path("/{orderId}")
    public Response updateOrder(@PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId,
            @Valid OrderRequestDTO request) {
        OrderResponseDTO order = orderService.updateOrder(companyId, orderId, request);
        return Response.ok(order).build();
    }

    @PATCH
    @Path("/{orderId}/status")
    public Response updateOrderStatus(@PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId,
            @Valid OrderStatusUpdateDTO request) {
        orderService.updateOrderStatus(companyId, orderId, request.getStatus());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{orderId}")
    public Response deleteOrder(@PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId) {
        orderService.deleteOrder(companyId, orderId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{orderId}/accept")
    public Response acceptOrder(@PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId) {
        orderService.acceptOrderWithStockReduction(companyId, orderId);
        return Response.noContent().build();
    }
}

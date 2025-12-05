package com.fulfai.partner.order;

import com.fulfai.common.dto.PaginatedResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/company/{companyId}/order")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService orderService;

    @POST
    public Response createOrder(@PathParam("companyId") String companyId, @Valid OrderRequestDTO request) {
        OrderResponseDTO createdOrder = orderService.createOrder(companyId, request);
        return Response.status(Response.Status.CREATED).entity(createdOrder).build();
    }

    @GET
    public Response getOrders(@PathParam("companyId") String companyId,
            @QueryParam("date") String date,
            @QueryParam("status") String status,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("nextToken") String nextToken,
            @QueryParam("limit") @DefaultValue("20") Integer limit) {

        PaginatedResponse<OrderResponseDTO> orders;

        if (date != null && !date.isEmpty() && status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByDateAndStatus(companyId, date, status, nextToken, limit);
        } else if (date != null && !date.isEmpty()) {
            orders = orderService.getOrdersByDate(companyId, date, nextToken, limit);
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            orders = orderService.getOrdersByDateRange(companyId, startDate, endDate, nextToken, limit);
        } else {
            orders = orderService.getOrdersByCompanyId(companyId, nextToken, limit);
        }

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
        OrderResponseDTO order = orderService.updateOrderStatus(companyId, orderId, request.getStatus());
        return Response.ok(order).build();
    }

    @DELETE
    @Path("/{orderId}")
    public Response deleteOrder(@PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId) {
        orderService.deleteOrder(companyId, orderId);
        return Response.noContent().build();
    }
}

package com.fulfai.sellingpartner.order;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.security.identity.SecurityIdentity;
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

        @Inject
        SecurityIdentity securityIdentity;


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

        String actorId = securityIdentity.getAttribute("sub");
        OrderActorRole actorRole = resolveActorRole(actorId, companyId, null);

        OrderResponseDTO order = orderService.getOrderForActor(companyId, orderId, actorRole);

        return Response.ok(order).build();
    }


    // =========================
    // ACTIONS (STATE MACHINE)
    // =========================

    @POST
    @Path("/{orderId}/actions")
    public Response applyOrderAction(
            @PathParam("companyId") String companyId,
            @PathParam("orderId") String orderId,
            @Valid OrderActionRequestDTO request
    ) {
        String actorId = securityIdentity.getAttribute("sub");
        OrderActorRole actorRole = resolveActorRole(actorId, companyId, null);

        OrderResponseDTO response = orderService.applyAction(
                companyId,
                orderId,
                request,
                actorId,
                actorRole);

        return Response.ok(response).build();
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

        private OrderActorRole resolveActorRole(String actorId, String companyId, String branchId) {
                if (securityIdentity.hasRole("admin") || securityIdentity.hasRole("ADMIN")) {
                        return OrderActorRole.ADMIN;
                }
                if (securityIdentity.hasRole("ops") || securityIdentity.hasRole("OPS")) {
                        return OrderActorRole.OPS;
                }
                return orderService.resolveSellerActorRole(actorId, companyId, branchId);
        }

}

package com.fulfai.sellingpartner.publicapi;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.order.OrderResponseDTO;
import com.fulfai.sellingpartner.order.OrderService;
import com.fulfai.sellingpartner.publicapi.dto.CreateOrderRequest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/public/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicOrderResource {

    @Inject
    OrderService orderService;

    @Context
    SecurityContext securityContext;



    // =====================================================
    // CREATE ORDER (LOGIN REQUIRED)
    // =====================================================

    /**
     * POST /api/selling-partner/public/orders
     */
    @POST
    @RolesAllowed("customer")
    public OrderResponseDTO createOrder(CreateOrderRequest request) {

        String userId = extractUserId();

        return orderService.createPublicOrder(
                userId,
                request
        );
    }



    // =====================================================
    // GET MY ORDERS (LOGIN REQUIRED)
    // =====================================================

    /**
     * GET /api/selling-partner/public/orders/me
     */
    @GET
    @Path("/me")
    @RolesAllowed("customer")
    public PaginatedResponse<OrderResponseDTO> getMyOrders(

            @QueryParam("nextToken")
            String nextToken,

            @QueryParam("limit")
            @DefaultValue("20")
            Integer limit

    ) {

        String userId = extractUserId();

        return orderService.getOrdersByUser(
                userId,
                nextToken,
                limit
        );
    }



    // =====================================================
    // GET ORDER BY ID (LOGIN REQUIRED)
    // =====================================================

    /**
     * GET /api/selling-partner/public/orders/{orderId}?companyId=...
     */
    @GET
    @Path("/{orderId}")
    @RolesAllowed("customer")
    public OrderResponseDTO getOrderById(

            @PathParam("orderId")
            String orderId,

            @QueryParam("companyId")
            String companyId

    ) {

        String userId = extractUserId();

        OrderResponseDTO order =
                orderService.getOrderForUser(
                        userId,
                        companyId,
                        orderId
                );

        if (order == null) {

            throw new NotFoundException(
                    "Order not found"
            );
        }

        return order;
    }



    // =====================================================
    // HELPER
    // =====================================================

    private String extractUserId() {

        if (securityContext == null
                || securityContext.getUserPrincipal() == null
                || securityContext.getUserPrincipal().getName() == null
                || securityContext.getUserPrincipal().getName().equals("ANONYMOUS")) {

            throw new NotAuthorizedException("Login required");
        }

        return securityContext
                .getUserPrincipal()
                .getName();
    }

}

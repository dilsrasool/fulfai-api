package com.fulfai.sellingpartner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.account.AccountService;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleRepository;
import com.fulfai.sellingpartner.product.Product;
import com.fulfai.sellingpartner.product.ProductRepository;
import com.fulfai.sellingpartner.publicapi.dto.CreateOrderRequest;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import static com.fulfai.sellingpartner.order.OrderWorkflow.OrderAction;

@ApplicationScoped
public class OrderService {

        private static final String DEFAULT_STATUS = FulfillmentStatus.CREATED.value();
        private static final String DEFAULT_PAYMENT_STATUS = PaymentStatus.PAYMENT_AUTHORIZED.value();

    @Inject
    AccountService accountService;

    @Inject
    OrderRepository orderRepository;

    @Inject
    ProductRepository productRepository;

        @Inject
        UserCompanyRoleRepository userCompanyRoleRepository;

    @Inject
    OrderMapper orderMapper;


    // =====================================================
    // PUBLIC ORDER CREATION
    // =====================================================

    public OrderResponseDTO createPublicOrder(
            String userId,
            CreateOrderRequest request
    ) {

        if (request.companyId == null || request.branchId == null)
            throw new BadRequestException("companyId and branchId required");

        if (request.items == null || request.items.isEmpty())
            throw new BadRequestException("Order must contain items");

        Order order = new Order();

        Instant now = Instant.now();

        order.setCompanyId(request.companyId);
        order.setBranchId(request.branchId);
        order.setUserId(userId);
        order.setDeliveryAddress(request.deliveryAddress);
        order.setOrderId(UUID.randomUUID().toString());
        order.setOrderDate(now);
        order.setStatus(DEFAULT_STATUS);
        order.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
        order.setEtaAt(now.plus(45, ChronoUnit.MINUTES));
        order.setSlaDeadlineAt(now.plus(90, ChronoUnit.MINUTES));
        order.setTimelineEvents(new ArrayList<>());
        order.setProcessedIdempotencyKeys(new ArrayList<>());
        order.setWorkflowMetadata(new HashMap<>());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        BigDecimal subtotal = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest reqItem : request.items) {

            Product product =
                    productRepository.getById(
                            request.companyId,
                            request.branchId,
                            reqItem.productId
                    );

            if (product == null)
                throw new NotFoundException("Product not found");

            if (product.getStockQuantity() < reqItem.quantity)
                throw new BadRequestException("Insufficient stock");

            OrderItem item = new OrderItem();

            item.setProductId(product.getProductId());
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(reqItem.quantity);

            BigDecimal total =
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(reqItem.quantity));

            item.setTotalPrice(total);

            subtotal = subtotal.add(total);

            orderItems.add(item);
        }

        order.setItems(orderItems);

        order.setSubtotal(subtotal);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);

        orderRepository.save(order);

        appendEvent(
                order,
                "create",
                userId,
                OrderActorRole.CUSTOMER,
                null,
                FulfillmentStatus.fromValue(order.getStatus()),
                null,
                "Order created",
                "create-" + order.getOrderId(),
                Map.of("origin", "public"));
        orderRepository.save(order);

        Log.debugf("Public order created: %s", order.getOrderId());

        return toResponseWithActions(order, OrderActorRole.CUSTOMER);
    }


    // =====================================================
    // GET USER ORDERS
    // =====================================================

    public PaginatedResponse<OrderResponseDTO> getOrdersByUser(
            String userId,
            String nextToken,
            Integer limit
    ) {

        PaginatedResponse<Order> response =
                orderRepository.getByUserId(
                        userId,
                        nextToken,
                        limit
                );

        return PaginatedResponse.<OrderResponseDTO>builder()
                .items(
                        response.getItems()
                                .stream()
                                .map(order -> toResponseWithActions(order, OrderActorRole.CUSTOMER))
                                .collect(Collectors.toList())
                )
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }


    // =====================================================
    // GET ORDER FOR USER (SECURE)
    // =====================================================

    public OrderResponseDTO getOrderForUser(
            String userId,
            String companyId,
            String orderId
    ) {

        Order order =
                orderRepository.getById(
                        companyId,
                        orderId
                );

        if (order == null)
            throw new NotFoundException("Order not found");

        if (!userId.equals(order.getUserId()))
            throw new NotFoundException("Order not found");

                return toResponseWithActions(order, OrderActorRole.CUSTOMER);
    }



    // =====================================================
    // GET ORDER BY ID (SELLER)
    // =====================================================

    public OrderResponseDTO getOrderById(
            String companyId,
            String orderId
    ) {

        Order order =
                orderRepository.getById(companyId, orderId);

        if (order == null)
            throw new NotFoundException("Order not found");

                return toResponseWithActions(order, OrderActorRole.VENDOR);
    }



    // =====================================================
    // DATE RANGE
    // =====================================================

    public PaginatedResponse<OrderResponseDTO> getOrdersByDateRange(
            String companyId,
            Instant startDate,
            Instant endDate,
            String nextToken,
            Integer limit
    ) {

        PaginatedResponse<Order> response =
                orderRepository.getByDateRange(
                        companyId,
                        startDate,
                        endDate,
                        nextToken,
                        limit
                );

        return PaginatedResponse.<OrderResponseDTO>builder()
                .items(
                        response.getItems()
                                .stream()
                                .map(order -> toResponseWithActions(order, OrderActorRole.VENDOR))
                                .collect(Collectors.toList())
                )
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }



    // =====================================================
    // UPDATE STATUS
    // =====================================================

    public void updateOrderStatus(
            String companyId,
            String orderId,
            String newStatus
    ) {
        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("mark_delivered");
        request.setTargetStatus(newStatus);
        request.setIdempotencyKey("legacy-status-update-" + orderId + "-" + newStatus);
        request.setNote("legacy status update endpoint");

        applyAction(companyId, orderId, request, "system", OrderActorRole.ADMIN);
    }



    private void updateAccountBalanceOnDelivery(
            String companyId,
            String orderId
    ) {

        Order order =
                orderRepository.getById(companyId, orderId);

        if (order != null)
            accountService.addToBalance(
                    companyId,
                    null,
                    order.getTotalAmount(),
                    orderId
            );
    }



    // =====================================================
    // DELETE
    // =====================================================

    public void deleteOrder(
            String companyId,
            String orderId
    ) {

        orderRepository.delete(companyId, orderId);
    }



    // =====================================================
    // ACCEPT ORDER
    // =====================================================

    public void acceptOrderWithStockReduction(
            String companyId,
            String orderId
    ) {
        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("accept");
        request.setIdempotencyKey("legacy-accept-" + orderId);
        request.setNote("legacy accept endpoint");
        applyAction(companyId, orderId, request, "system", OrderActorRole.VENDOR);
    }

public OrderResponseDTO createOrder(
        String companyId,
        OrderRequestDTO request
) {

    if (request == null)
        throw new BadRequestException("Order request is null");


    Order order = orderMapper.toEntity(request);

    if (request.getUserId() != null) {
        order.setUserId(request.getUserId());
    }

    Instant now = Instant.now();


    order.setCompanyId(companyId);

    order.setOrderId(UUID.randomUUID().toString());

    order.setOrderDate(now);

    order.setStatus(DEFAULT_STATUS);

    order.setPaymentStatus(DEFAULT_PAYMENT_STATUS);

        order.setEtaAt(now.plus(45, ChronoUnit.MINUTES));

        order.setSlaDeadlineAt(now.plus(90, ChronoUnit.MINUTES));

        order.setTimelineEvents(new ArrayList<>());

        order.setProcessedIdempotencyKeys(new ArrayList<>());

        order.setWorkflowMetadata(new HashMap<>());

    order.setCreatedAt(now);

    order.setUpdatedAt(now);



    // calculate totals safely

    BigDecimal subtotal = BigDecimal.ZERO;


    if (order.getItems() != null) {

        for (OrderItem item : order.getItems()) {

            BigDecimal totalPrice =
                    item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));


            item.setTotalPrice(totalPrice);

            subtotal = subtotal.add(totalPrice);

        }

    }


    order.setSubtotal(subtotal);

    order.setTaxAmount(BigDecimal.ZERO);

    order.setDiscountAmount(BigDecimal.ZERO);

    order.setTotalAmount(subtotal);



    orderRepository.save(order);

    appendEvent(
            order,
            "create",
            "seller",
            OrderActorRole.VENDOR,
            null,
            FulfillmentStatus.fromValue(order.getStatus()),
            null,
            "Seller created order",
            "create-" + order.getOrderId(),
            Map.of("origin", "seller"));
    orderRepository.save(order);


    Log.debugf(
            "Seller order created: %s",
            order.getOrderId()
    );


        return toResponseWithActions(order, OrderActorRole.VENDOR);

}

public OrderResponseDTO updateOrder(
        String companyId,
        String orderId,
        OrderRequestDTO request
) {

    if (request == null)
        throw new BadRequestException("Order request is null");


    Order existing =
            orderRepository.getById(companyId, orderId);


    if (existing == null)
        throw new NotFoundException("Order not found");



    Order updated =
            orderMapper.toEntity(request);

    if (request.getUserId() != null) {
        updated.setUserId(request.getUserId());
    } else {
        updated.setUserId(existing.getUserId());
    }

    if (request.getDeliveryAddress() != null) {
        updated.setDeliveryAddress(request.getDeliveryAddress());
    } else {
        updated.setDeliveryAddress(existing.getDeliveryAddress());
    }

    updated.setCompanyId(companyId);

    updated.setOrderId(orderId);

    updated.setOrderDate(existing.getOrderDate());

    updated.setStatus(existing.getStatus());

    updated.setPaymentStatus(
            request.getPaymentStatus() != null
                    ? request.getPaymentStatus()
                    : existing.getPaymentStatus()
    );

    updated.setIssueStatus(existing.getIssueStatus());

    updated.setEtaAt(existing.getEtaAt());

    updated.setSlaDeadlineAt(existing.getSlaDeadlineAt());

    updated.setTimelineEvents(existing.getTimelineEvents());

    updated.setProcessedIdempotencyKeys(existing.getProcessedIdempotencyKeys());

    updated.setWorkflowMetadata(existing.getWorkflowMetadata());

    updated.setCreatedAt(existing.getCreatedAt());

    updated.setUpdatedAt(Instant.now());



    // calculate totals safely

    BigDecimal subtotal = BigDecimal.ZERO;


    if (updated.getItems() != null) {

        for (OrderItem item : updated.getItems()) {

            BigDecimal totalPrice =
                    item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

            item.setTotalPrice(totalPrice);

            subtotal = subtotal.add(totalPrice);

        }

    }


    updated.setSubtotal(subtotal);

    updated.setTaxAmount(BigDecimal.ZERO);

    updated.setDiscountAmount(BigDecimal.ZERO);

    updated.setTotalAmount(subtotal);



    orderRepository.save(updated);



    Log.debugf(
            "Order updated: %s",
            orderId
    );



        return toResponseWithActions(updated, OrderActorRole.VENDOR);

}

        public OrderResponseDTO applyAction(
                        String companyId,
                        String orderId,
                        OrderActionRequestDTO request,
                        String actorId,
                        OrderActorRole actorRole
        ) {

                if (request == null) {
                        throw new BadRequestException("Action request is required");
                }

                if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
                        throw new BadRequestException("idempotencyKey is required");
                }

                Order order = orderRepository.getById(companyId, orderId);
                if (order == null) {
                        throw new NotFoundException("Order not found");
                }

                if (order.getProcessedIdempotencyKeys() != null && order.getProcessedIdempotencyKeys().contains(request.getIdempotencyKey())) {
                        return toResponseWithActions(order, actorRole);
                }

                String requestedAction = request.getAction();
                String currentStatus = order.getStatus();
                OrderAction action = null;
                FulfillmentStatus current = null;

                try {
                        action = parseAction(requestedAction);
                        OrderWorkflow.validateActionPermission(action, actorRole);

                        current = safeStatus(currentStatus);
                        OrderWorkflow.validateActionAllowedForState(current, action);
                        FulfillmentStatus next = resolveTargetStatus(action, current, request);

                        validateReasonCode(action, request.getReasonCode());

                        if (next != null && next != current) {
                                OrderWorkflow.validateTransition(current, next);
                                order.setStatus(next.value());
                        }

                        if (action == OrderAction.ACCEPT) {
                                reduceStockForOrder(order);
                        }

                        applyIssueAndPaymentChanges(order, action, request.getReasonCode());

                        if (action == OrderAction.MARK_DELIVERED && current != FulfillmentStatus.DELIVERED) {
                                updateAccountBalanceOnDelivery(companyId, orderId);
                        }

                        if (order.getProcessedIdempotencyKeys() == null) {
                                order.setProcessedIdempotencyKeys(new ArrayList<>());
                        }
                        order.getProcessedIdempotencyKeys().add(request.getIdempotencyKey());

                        if (order.getWorkflowMetadata() == null) {
                                order.setWorkflowMetadata(new HashMap<>());
                        }
                        if (request.getMetadata() != null) {
                                order.getWorkflowMetadata().putAll(request.getMetadata());
                        }

                        appendEvent(
                                        order,
                                        action.name().toLowerCase(),
                                        actorId,
                                        actorRole,
                                        current,
                                        next,
                                        request.getReasonCode(),
                                        request.getNote(),
                                        request.getIdempotencyKey(),
                                        request.getMetadata());

                        order.setUpdatedAt(Instant.now());
                        orderRepository.save(order);

                        Log.infof(
                                        "ORDER_ACTION company=%s order=%s actorRole=%s action=%s from=%s to=%s key=%s",
                                        companyId,
                                        orderId,
                                        actorRole,
                                        action,
                                        current.value(),
                                        next == null ? "-" : next.value(),
                                        request.getIdempotencyKey());

                        return toResponseWithActions(order, actorRole);
                } catch (OrderWorkflowException ex) {
                        Log.warnf(
                                        "ORDER_ACTION_REJECT actorRole=%s company=%s order=%s status=%s action=%s rejectionCode=%s",
                                        actorRole,
                                        companyId,
                                        orderId,
                                        current == null ? currentStatus : current.value(),
                                        action == null ? requestedAction : action.name().toLowerCase(),
                                        ex.getCode());
                        throw ex;
                }
        }

        public OrderActorRole resolveSellerActorRole(String actorId, String companyId, String branchId) {
                if (actorId == null || actorId.isBlank()) {
                        throw new OrderWorkflowException(Response.Status.UNAUTHORIZED, "UNAUTHENTICATED", "Authenticated user is required");
                }

                UserCompanyRole companyRole = userCompanyRoleRepository.getRole(actorId, companyId, null);
                UserCompanyRole branchRole = branchId == null ? null : userCompanyRoleRepository.getRole(actorId, companyId, branchId);
                if (companyRole != null || branchRole != null || userCompanyRoleRepository.hasAnyRoleInCompany(actorId, companyId)) {
                        return OrderActorRole.VENDOR;
                }

                throw new OrderWorkflowException(Response.Status.FORBIDDEN, "ACTION_FORBIDDEN", "User is not assigned to this company");
        }

        public OrderResponseDTO getOrderForActor(String companyId, String orderId, OrderActorRole actorRole) {
                Order order = orderRepository.getById(companyId, orderId);
                if (order == null) {
                        throw new NotFoundException("Order not found");
                }
                return toResponseWithActions(order, actorRole);
        }

        private void reduceStockForOrder(Order order) {
                if (order.getItems() == null) {
                        return;
                }

                for (OrderItem item : order.getItems()) {
                        Product product = productRepository.getById(order.getCompanyId(), order.getBranchId(), item.getProductId());
                        if (product == null) {
                                throw new NotFoundException("Product not found for item " + item.getProductId());
                        }
                        if (product.getStockQuantity() < item.getQuantity()) {
                                throw new OrderWorkflowException(Response.Status.CONFLICT, "INSUFFICIENT_STOCK", "Insufficient stock for product " + item.getProductId());
                        }

                        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                        productRepository.save(product);
                }
        }

        private void applyIssueAndPaymentChanges(Order order, OrderAction action, String reasonCode) {
                switch (action) {
                        case CREATE_ISSUE -> order.setIssueStatus(IssueStatus.ISSUE_REPORTED.value());
                        case RESOLVE_ISSUE_REFUND -> {
                                order.setIssueStatus(IssueStatus.RESOLVED_REFUND.value());
                                order.setPaymentStatus(PaymentStatus.REFUND_PENDING.value());
                                order.setStatus(FulfillmentStatus.REFUNDED.value());
                        }
                        case RESOLVE_ISSUE_REDELIVERY -> order.setIssueStatus(IssueStatus.RESOLVED_REDELIVERY.value());
                        case RESOLVE_ISSUE_REPLACEMENT -> order.setIssueStatus(IssueStatus.RESOLVED_REPLACEMENT.value());
                        case REJECT_CLAIM -> order.setIssueStatus(IssueStatus.REJECTED_CLAIM.value());
                        case MARK_DELIVERED -> {
                                if (PaymentStatus.fromValue(order.getPaymentStatus()) == PaymentStatus.PAYMENT_AUTHORIZED) {
                                        order.setPaymentStatus(PaymentStatus.PAYMENT_CAPTURED.value());
                                }
                        }
                        case DECIDE_CANCEL -> {
                                if (FulfillmentStatus.CANCELLED.value().equalsIgnoreCase(order.getStatus())
                                                && OrderReasonCode.REFUND_REASON_CODES.contains(OrderReasonCode.fromNullable(reasonCode))) {
                                        order.setPaymentStatus(PaymentStatus.REFUND_PENDING.value());
                                }
                        }
                        case MARK_REFUNDED -> order.setPaymentStatus(PaymentStatus.REFUNDED.value());
                        default -> {
                        }
                }
        }

        private FulfillmentStatus resolveTargetStatus(OrderAction action, FulfillmentStatus current, OrderActionRequestDTO request) {
                return switch (action) {
                        case ACCEPT -> FulfillmentStatus.ACCEPTED;
                        case REJECT -> FulfillmentStatus.CANCELLED;
                        case REQUEST_CHANGE -> current;
                        case APPROVE_CHANGE -> current;
                        case REJECT_CHANGE -> fallbackStatus(request.getTargetStatus(), FulfillmentStatus.ACCEPTED);
                        case REQUEST_CANCEL -> FulfillmentStatus.CANCELLED;
                        case DECIDE_CANCEL -> fallbackStatus(request.getTargetStatus(), FulfillmentStatus.CANCELLED);
                        case MARK_PREPARING -> FulfillmentStatus.PREPARING;
                        case MARK_READY -> FulfillmentStatus.READY;
                        case MARK_PICKED_UP -> FulfillmentStatus.PICKED_UP;
                        case MARK_ON_THE_WAY -> FulfillmentStatus.ON_THE_WAY;
                        case MARK_DELIVERED -> FulfillmentStatus.DELIVERED;
                        case MARK_FAILED -> FulfillmentStatus.FAILED;
                        case MARK_RETURNED -> FulfillmentStatus.RETURNED;
                        case MARK_REFUNDED -> FulfillmentStatus.REFUNDED;
                        case CREATE_ISSUE, RESOLVE_ISSUE_REFUND, RESOLVE_ISSUE_REDELIVERY, RESOLVE_ISSUE_REPLACEMENT, REJECT_CLAIM ->
                                        fallbackStatus(request.getTargetStatus(), current);
                };
        }

        private FulfillmentStatus fallbackStatus(String requested, FulfillmentStatus fallback) {
                FulfillmentStatus parsed = FulfillmentStatus.fromValue(requested);
                return parsed == null ? fallback : parsed;
        }

        private void validateReasonCode(OrderAction action, String reasonCodeRaw) {
                OrderReasonCode reasonCode;
                try {
                        reasonCode = OrderReasonCode.fromNullable(reasonCodeRaw);
                } catch (IllegalArgumentException ex) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_REASON_CODE", "Unknown reasonCode: " + reasonCodeRaw);
                }

                if ((action == OrderAction.REJECT || action == OrderAction.REQUEST_CANCEL || action == OrderAction.DECIDE_CANCEL
                                || action == OrderAction.REQUEST_CHANGE || action == OrderAction.APPROVE_CHANGE
                                || action == OrderAction.REJECT_CHANGE || action == OrderAction.RESOLVE_ISSUE_REFUND)
                                && reasonCode == null) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "REASON_CODE_REQUIRED", "reasonCode is required for action " + action.name().toLowerCase());
                }

                if (action == OrderAction.REJECT && !OrderReasonCode.REJECT_REASON_CODES.contains(reasonCode)) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_REASON_CODE", "reasonCode is not valid for reject action");
                }
                if ((action == OrderAction.REQUEST_CANCEL || action == OrderAction.DECIDE_CANCEL)
                                && !OrderReasonCode.CANCEL_REASON_CODES.contains(reasonCode)
                                && (reasonCode == null || !OrderReasonCode.REFUND_REASON_CODES.contains(reasonCode))) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_REASON_CODE", "reasonCode is not valid for cancel action");
                }
                if ((action == OrderAction.REQUEST_CHANGE || action == OrderAction.APPROVE_CHANGE || action == OrderAction.REJECT_CHANGE)
                                && !OrderReasonCode.CHANGE_REASON_CODES.contains(reasonCode)) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_REASON_CODE", "reasonCode is not valid for change action");
                }
                if ((action == OrderAction.RESOLVE_ISSUE_REFUND || action == OrderAction.MARK_REFUNDED)
                                && !OrderReasonCode.REFUND_REASON_CODES.contains(reasonCode)) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_REASON_CODE", "reasonCode is not valid for refund resolution");
                }
        }

        private FulfillmentStatus safeStatus(String raw) {
                FulfillmentStatus status = FulfillmentStatus.fromValue(raw);
                if (status == null) {
                        throw new OrderWorkflowException(Response.Status.CONFLICT, "INVALID_ORDER_STATE", "Order has unknown status: " + raw);
                }
                return status;
        }

        private OrderAction parseAction(String raw) {
                if (raw == null || raw.isBlank()) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_ACTION", "action is required");
                }

                String normalized = raw.trim().toUpperCase(Locale.ROOT);
                normalized = normalized.replace('-', '_');

                try {
                        return OrderAction.valueOf(normalized);
                } catch (IllegalArgumentException ex) {
                        throw new OrderWorkflowException(Response.Status.BAD_REQUEST, "INVALID_ACTION", "Unsupported action: " + raw);
                }
        }

        private void appendEvent(
                        Order order,
                        String action,
                        String actorId,
                        OrderActorRole actorRole,
                        FulfillmentStatus from,
                        FulfillmentStatus to,
                        String reasonCode,
                        String note,
                        String idempotencyKey,
                        Map<String, String> metadata
        ) {
                if (order.getTimelineEvents() == null) {
                        order.setTimelineEvents(new ArrayList<>());
                }

                OrderTimelineEvent event = new OrderTimelineEvent();
                event.setEventId(UUID.randomUUID().toString());
                event.setAction(action);
                event.setActorId(actorId);
                event.setActorRole(actorRole.name().toLowerCase());
                event.setFromStatus(from == null ? null : from.value());
                event.setToStatus(to == null ? null : to.value());
                event.setReasonCode(reasonCode);
                event.setNote(note);
                event.setIdempotencyKey(idempotencyKey);
                event.setMetadata(metadata == null ? Map.of() : metadata);
                event.setTimestamp(Instant.now());

                order.getTimelineEvents().add(event);
        }

        private OrderResponseDTO toResponseWithActions(Order order, OrderActorRole actorRole) {
                OrderResponseDTO response = orderMapper.toResponseDTO(order);
                FulfillmentStatus current = FulfillmentStatus.fromValue(order.getStatus());
                response.setNextAllowedActions(current == null
                                ? Set.of()
                                : OrderWorkflow.allowedActionNamesForActor(current, actorRole));
                return response;
        }


}

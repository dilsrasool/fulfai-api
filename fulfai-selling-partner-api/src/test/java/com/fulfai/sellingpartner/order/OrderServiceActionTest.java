package com.fulfai.sellingpartner.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRoleRepository;
import com.fulfai.sellingpartner.account.AccountService;
import com.fulfai.sellingpartner.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceActionTest {

    @Mock
    AccountService accountService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    UserCompanyRoleRepository userCompanyRoleRepository;

    @Mock
    OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setCompanyId("c1");
        order.setOrderId("o1");
        order.setBranchId("b1");
        order.setStatus(FulfillmentStatus.CREATED.value());
        order.setPaymentStatus(PaymentStatus.PAYMENT_AUTHORIZED.value());
        order.setTotalAmount(BigDecimal.TEN);
        order.setItems(new ArrayList<>());
        order.setTimelineEvents(new ArrayList<>());
        order.setProcessedIdempotencyKeys(new ArrayList<>());
        order.setWorkflowMetadata(new HashMap<>());
        order.setCreatedAt(Instant.now());

        OrderResponseDTO mapped = new OrderResponseDTO();
        mapped.setCompanyId("c1");
        mapped.setOrderId("o1");

        when(orderRepository.getById("c1", "o1")).thenReturn(order);
        lenient().when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(mapped);
    }

    @Test
    void shouldBeIdempotentForDuplicateKey() {
        order.getProcessedIdempotencyKeys().add("dup-key");

        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("accept");
        request.setIdempotencyKey("dup-key");
        request.setReasonCode(OrderReasonCode.VENDOR_REJECTED_CLOSED.name());

        OrderResponseDTO response = orderService.applyAction(
                "c1",
                "o1",
                request,
                "user-1",
                OrderActorRole.VENDOR);

        assertNotNull(response);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldRequireReasonCodeForReject() {
        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("reject");
        request.setIdempotencyKey("k1");

        assertThrows(
                OrderWorkflowException.class,
                () -> orderService.applyAction("c1", "o1", request, "user-1", OrderActorRole.VENDOR));
    }

    @Test
    void shouldRequestRefundWhenCancelDecisionUsesRefundReason() {
        order.setStatus(FulfillmentStatus.CREATED.value());

        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("decide_cancel");
        request.setTargetStatus(FulfillmentStatus.CANCELLED.value());
        request.setReasonCode(OrderReasonCode.REFUND_CUSTOMER_REQUEST.name());
        request.setIdempotencyKey("cancel-k1");

        OrderResponseDTO response = orderService.applyAction(
                "c1",
                "o1",
                request,
                "admin-1",
                OrderActorRole.ADMIN);

        assertNotNull(response);
        assertEquals(FulfillmentStatus.CANCELLED.value(), order.getStatus());
        assertEquals(PaymentStatus.REFUND_PENDING.value(), order.getPaymentStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectInvalidTransition() {
        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("mark_delivered");
        request.setIdempotencyKey("k2");

        assertThrows(
                OrderWorkflowException.class,
                () -> orderService.applyAction("c1", "o1", request, "ops-1", OrderActorRole.OPS));
    }

    @Test
    void shouldCreateIssueWithoutChangingFulfillmentStatus() {
        order.setStatus(FulfillmentStatus.DELIVERED.value());

        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("create_issue");
        request.setIdempotencyKey("issue-1");
        request.setReasonCode(OrderReasonCode.ISSUE_MISSING_ITEM.name());

        orderService.applyAction("c1", "o1", request, "customer-1", OrderActorRole.CUSTOMER);

        assertEquals(FulfillmentStatus.DELIVERED.value(), order.getStatus());
        assertEquals(IssueStatus.ISSUE_REPORTED.value(), order.getIssueStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectActionWhenNotAllowedForCurrentState() {
        order.setStatus(FulfillmentStatus.DELIVERED.value());

        OrderActionRequestDTO request = new OrderActionRequestDTO();
        request.setAction("request_change");
        request.setIdempotencyKey("state-k1");
        request.setReasonCode(OrderReasonCode.CUSTOMER_CHANGE_REQUEST.name());

        OrderWorkflowException ex = assertThrows(
                OrderWorkflowException.class,
                () -> orderService.applyAction("c1", "o1", request, "vendor-1", OrderActorRole.VENDOR));

        assertEquals(409, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("is not allowed when order status is"));
    }
}

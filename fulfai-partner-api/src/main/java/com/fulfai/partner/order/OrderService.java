package com.fulfai.partner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class OrderService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final String DEFAULT_STATUS = "PENDING";
    private static final String DEFAULT_PAYMENT_STATUS = "PENDING";

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderMapper orderMapper;

    public OrderResponseDTO createOrder(String companyId, @Valid OrderRequestDTO orderDTO) {
        Order order = orderMapper.toEntity(orderDTO);

        Instant now = Instant.now();
        String orderDate = LocalDate.now().format(DATE_FORMAT);
        String status = DEFAULT_STATUS;

        order.setCompanyId(companyId);
        order.setOrderId(UUID.randomUUID().toString());
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setDateStatusKey(orderDate + "#" + status);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
        }

        // Calculate totals if not provided
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getTotalPrice() == null && item.getQuantity() != null && item.getUnitPrice() != null) {
                    item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }

            if (order.getSubtotal() == null) {
                order.setSubtotal(order.getItems().stream()
                        .map(OrderItem::getTotalPrice)
                        .filter(p -> p != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        }

        if (order.getTotalAmount() == null) {
            BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
            BigDecimal tax = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            order.setTotalAmount(subtotal.add(tax).subtract(discount));
        }

        orderRepository.save(order);
        Log.debugf("Created order with id: %s for company: %s", order.getOrderId(), companyId);

        return orderMapper.toResponseDTO(order);
    }

    public OrderResponseDTO getOrderById(String companyId, String orderId) {
        Log.debugf("Getting order by companyId: %s, orderId: %s", companyId, orderId);
        Order order = orderRepository.getById(companyId, orderId);
        if (order != null) {
            return orderMapper.toResponseDTO(order);
        } else {
            throw new NotFoundException("Order not found with id: " + orderId);
        }
    }

    public PaginatedResponse<OrderResponseDTO> getOrdersByCompanyId(String companyId, String nextToken, Integer limit) {
        Log.debugf("Getting orders for company: %s", companyId);
        PaginatedResponse<Order> response = orderRepository.getByCompanyId(companyId, nextToken, limit);

        return PaginatedResponse.<OrderResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(orderMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<OrderResponseDTO> getOrdersByDateAndStatus(String companyId, String date, String status,
            String nextToken, Integer limit) {
        Log.debugf("Getting orders for company: %s, date: %s, status: %s", companyId, date, status);
        PaginatedResponse<Order> response = orderRepository.getByDateAndStatus(companyId, date, status, nextToken, limit);

        return PaginatedResponse.<OrderResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(orderMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<OrderResponseDTO> getOrdersByDate(String companyId, String date,
            String nextToken, Integer limit) {
        Log.debugf("Getting orders for company: %s, date: %s", companyId, date);
        PaginatedResponse<Order> response = orderRepository.getByDate(companyId, date, nextToken, limit);

        return PaginatedResponse.<OrderResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(orderMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public PaginatedResponse<OrderResponseDTO> getOrdersByDateRange(String companyId, String startDate, String endDate,
            String nextToken, Integer limit) {
        Log.debugf("Getting orders for company: %s, startDate: %s, endDate: %s", companyId, startDate, endDate);
        PaginatedResponse<Order> response = orderRepository.getByDateRange(companyId, startDate, endDate, nextToken, limit);

        return PaginatedResponse.<OrderResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(orderMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public OrderResponseDTO updateOrderStatus(String companyId, String orderId, String newStatus) {
        Order order = orderRepository.getById(companyId, orderId);
        if (order != null) {
            order.setStatus(newStatus);
            order.setDateStatusKey(order.getOrderDate() + "#" + newStatus);
            order.setUpdatedAt(Instant.now());

            orderRepository.save(order);
            Log.debugf("Updated order status to %s for order: %s", newStatus, orderId);

            return orderMapper.toResponseDTO(order);
        } else {
            throw new NotFoundException("Order not found with id: " + orderId);
        }
    }

    public OrderResponseDTO updateOrder(String companyId, String orderId, @Valid OrderRequestDTO orderDTO) {
        Order originalOrder = orderRepository.getById(companyId, orderId);
        if (originalOrder != null) {
            Order order = orderMapper.toEntity(orderDTO);
            order.setCompanyId(companyId);
            order.setOrderId(orderId);
            order.setOrderDate(originalOrder.getOrderDate());
            order.setStatus(originalOrder.getStatus());
            order.setDateStatusKey(originalOrder.getDateStatusKey());
            order.setCreatedAt(originalOrder.getCreatedAt());
            order.setUpdatedAt(Instant.now());

            // Calculate totals
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getTotalPrice() == null && item.getQuantity() != null && item.getUnitPrice() != null) {
                        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }

            orderRepository.save(order);
            Log.debugf("Updated order with id: %s", orderId);

            return orderMapper.toResponseDTO(order);
        } else {
            throw new NotFoundException("Order not found with id: " + orderId);
        }
    }

    public void deleteOrder(String companyId, String orderId) {
        Order order = orderRepository.getById(companyId, orderId);
        if (order != null) {
            orderRepository.delete(companyId, orderId);
            Log.debugf("Deleted order with id: %s", orderId);
        } else {
            throw new NotFoundException("Order not found with id: " + orderId);
        }
    }
}

package com.fulfai.partner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.partner.product.Product;
import com.fulfai.partner.product.ProductRepository;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ApplicationScoped
public class OrderService {

    private static final String DEFAULT_STATUS = "RECEIVED";
    private static final String DEFAULT_PAYMENT_STATUS = "PENDING";

    @Inject
    OrderRepository orderRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    OrderMapper orderMapper;

    public OrderResponseDTO createOrder(String companyId, @Valid OrderRequestDTO orderDTO) {
        Order order = orderMapper.toEntity(orderDTO);

        Instant now = Instant.now();
        String status = DEFAULT_STATUS;

        order.setCompanyId(companyId);
        order.setOrderId(UUID.randomUUID().toString());
        order.setOrderDate(now);
        order.setStatus(status);
        order.setDateStatusKey(now.toString() + "#" + status);
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

    public PaginatedResponse<OrderResponseDTO> getOrdersByDateRange(String companyId, Instant startDate,
            Instant endDate, String nextToken, Integer limit) {
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

    public void updateOrderStatus(String companyId, String orderId, String newStatus) {
        // Validate the target status is valid
        OrderStatus targetStatus = OrderStatus.fromString(newStatus);
        if (targetStatus == null) {
            throw new BadRequestException("Invalid status: " + newStatus);
        }

        // Get allowed "from" statuses for this target status
        java.util.List<String> allowedFromStatuses = OrderStatus.getAllowedFromStatuses(newStatus);
        if (allowedFromStatuses.isEmpty()) {
            throw new BadRequestException("No valid transitions to status: " + newStatus);
        }

        // Use conditional update with condition check (no fetch required)
        // Condition: item exists AND current status is one of the allowed "from" statuses
        try {
            orderRepository.updateStatus(companyId, orderId, newStatus, allowedFromStatuses);
            Log.debugf("Updated order status to %s for order: %s", newStatus, orderId);
        } catch (software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException e) {
            throw new BadRequestException("Order not found or invalid status transition to: " + newStatus +
                    ". Allowed from: " + allowedFromStatuses);
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

    /**
     * Accept an order and reduce product stock quantities in a single transaction.
     * This ensures that:
     * 1. Order exists and status is RECEIVED (validated via condition)
     * 2. Order status transitions to ACCEPTED
     * 3. All product quantities are reduced atomically
     * 4. If any product has insufficient stock, the entire transaction fails
     */
    public void acceptOrderWithStockReduction(String companyId, String orderId) {
        // Fetch order to get items (we need product IDs and quantities)
        Order order = orderRepository.getById(companyId, orderId);
        if (order == null) {
            throw new NotFoundException("Order not found with id: " + orderId);
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("Order has no items");
        }

        // Fetch all products (needed to update them with reduced stock)
        Map<String, Product> productMap = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) {
                throw new BadRequestException("Order item missing productId");
            }
            Product product = productRepository.getById(companyId, item.getProductId());
            if (product == null) {
                throw new NotFoundException("Product not found: " + item.getProductId());
            }
            productMap.put(item.getProductId(), product);
        }

        // Prepare updated order
        order.setStatus(OrderStatus.ACCEPTED.name());
        order.setDateStatusKey(order.getOrderDate().toString() + "#" + OrderStatus.ACCEPTED.name());
        order.setUpdatedAt(Instant.now());

        // Execute transaction: update order status + reduce all product quantities
        // Conditions ensure: order status is RECEIVED, all products have sufficient stock
        try {
            DynamoDBUtils.transactWriteItems(orderRepository.getEnhancedClient(), builder -> {
                // Add order update with condition: exists AND status is RECEIVED
                builder.addPutItem(orderRepository.getOrderTable(),
                        software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest.builder(Order.class)
                                .item(order)
                                .conditionExpression(buildStatusCondition(OrderStatus.RECEIVED.name()))
                                .build());

                // Add product stock reductions with conditions
                for (OrderItem item : order.getItems()) {
                    Product product = productMap.get(item.getProductId());
                    Product updatedProduct = cloneProductWithReducedStock(product, item.getQuantity());

                    builder.addPutItem(productRepository.getProductTable(),
                            software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest.builder(Product.class)
                                    .item(updatedProduct)
                                    .conditionExpression(buildStockCondition(item.getQuantity()))
                                    .build());
                }
            });

            Log.debugf("Accepted order %s and reduced stock for %d products", orderId, order.getItems().size());

        } catch (DynamoDBUtils.TransactionFailedException e) {
            Log.errorf("Transaction failed for order %s: %s", orderId, e.getMessage());
            throw new BadRequestException("Failed to accept order. Order must be RECEIVED and all products must have sufficient stock.");
        }
    }

    /**
     * Build a condition expression to check order status.
     */
    private Expression buildStatusCondition(String expectedStatus) {
        return Expression.builder()
                .expression("attribute_exists(#pk) AND #status = :expectedStatus")
                .putExpressionName("#pk", "companyId")
                .putExpressionName("#status", "status")
                .putExpressionValue(":expectedStatus", AttributeValue.builder().s(expectedStatus).build())
                .build();
    }

    /**
     * Build a condition expression to check sufficient stock quantity.
     */
    private Expression buildStockCondition(int requiredQuantity) {
        return Expression.builder()
                .expression("attribute_exists(#pk) AND stockQuantity >= :requiredQty")
                .putExpressionName("#pk", "companyId")
                .putExpressionValue(":requiredQty", AttributeValue.builder().n(String.valueOf(requiredQuantity)).build())
                .build();
    }

    /**
     * Create a copy of product with reduced stock quantity.
     */
    private Product cloneProductWithReducedStock(Product original, int quantityToReduce) {
        Product updated = new Product();
        updated.setCompanyId(original.getCompanyId());
        updated.setProductId(original.getProductId());
        updated.setName(original.getName());
        updated.setDescription(original.getDescription());
        updated.setCategory(original.getCategory());
        updated.setSku(original.getSku());
        updated.setBarcode(original.getBarcode());
        updated.setPrice(original.getPrice());
        updated.setCostPrice(original.getCostPrice());
        updated.setUnit(original.getUnit());
        updated.setStockQuantity(original.getStockQuantity() - quantityToReduce);
        updated.setReorderLevel(original.getReorderLevel());
        updated.setImageUrl(original.getImageUrl());
        updated.setIsActive(original.getIsActive());
        updated.setCreatedAt(original.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        return updated;
    }
}

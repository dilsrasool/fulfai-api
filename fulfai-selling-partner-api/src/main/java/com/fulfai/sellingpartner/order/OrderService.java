package com.fulfai.sellingpartner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.account.AccountService;
import com.fulfai.sellingpartner.product.Product;
import com.fulfai.sellingpartner.product.ProductRepository;
import com.fulfai.sellingpartner.publicapi.dto.CreateOrderRequest;

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
    AccountService accountService;

    @Inject
    OrderRepository orderRepository;

    @Inject
    ProductRepository productRepository;

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
        order.setOrderId(UUID.randomUUID().toString());
        order.setOrderDate(now);
        order.setStatus(DEFAULT_STATUS);
        order.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
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

        Log.debugf("Public order created: %s", order.getOrderId());

        return orderMapper.toResponseDTO(order);
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
                                .map(orderMapper::toResponseDTO)
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

        return orderMapper.toResponseDTO(order);
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

        return orderMapper.toResponseDTO(order);
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
                                .map(orderMapper::toResponseDTO)
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

        List<String> allowed =
                OrderStatus.getAllowedFromStatuses(newStatus);

        orderRepository.updateStatus(
                companyId,
                orderId,
                newStatus,
                allowed
        );

        if (OrderStatus.DELIVERED.name().equals(newStatus))
            updateAccountBalanceOnDelivery(companyId, orderId);
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

        Order order =
                orderRepository.getById(companyId, orderId);

        if (order == null)
            throw new NotFoundException("Order not found");

        Map<String, Product> productMap = new HashMap<>();

        for (OrderItem item : order.getItems()) {

            Product product =
                    productRepository.getById(
                            companyId,
                            order.getBranchId(),
                            item.getProductId()
                    );

            productMap.put(item.getProductId(), product);
        }

        order.setStatus(OrderStatus.ACCEPTED.name());
        order.setUpdatedAt(Instant.now());

        DynamoDBUtils.transactWriteItems(
                orderRepository.getEnhancedClient(),
                builder -> {

                    builder.addPutItem(
                            orderRepository.getOrderTable(),
                            order
                    );

                    for (OrderItem item : order.getItems()) {

                        Product p =
                                productMap.get(item.getProductId());

                        p.setStockQuantity(
                                p.getStockQuantity()
                                        - item.getQuantity()
                        );

                        builder.addPutItem(
                                productRepository.getProductTable(),
                                p
                        );
                    }
                }
        );
    }

public OrderResponseDTO createOrder(
        String companyId,
        OrderRequestDTO request
) {

    if (request == null)
        throw new BadRequestException("Order request is null");


    Order order = orderMapper.toEntity(request);


    Instant now = Instant.now();


    order.setCompanyId(companyId);

    order.setOrderId(UUID.randomUUID().toString());

    order.setOrderDate(now);

    order.setStatus(DEFAULT_STATUS);

    order.setPaymentStatus(DEFAULT_PAYMENT_STATUS);

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


    Log.debugf(
            "Seller order created: %s",
            order.getOrderId()
    );


    return orderMapper.toResponseDTO(order);

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



    updated.setCompanyId(companyId);

    updated.setOrderId(orderId);

    updated.setOrderDate(existing.getOrderDate());

    updated.setStatus(existing.getStatus());

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



    return orderMapper.toResponseDTO(updated);

}


}

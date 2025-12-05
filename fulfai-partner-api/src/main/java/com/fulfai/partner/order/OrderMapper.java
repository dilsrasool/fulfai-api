package com.fulfai.partner.order;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dateStatusKey", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequestDTO dto);

    OrderResponseDTO toResponseDTO(Order entity);

    OrderItem toOrderItem(OrderItemDTO dto);

    OrderItemDTO toOrderItemDTO(OrderItem entity);

    List<OrderItem> toOrderItems(List<OrderItemDTO> dtos);

    List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> entities);
}

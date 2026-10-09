package com.sumit.flashsale.flash_sale_storefront.dto;




import com.sumit.flashsale.flash_sale_storefront.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponseDto(
        Long orderId,
        Long productId,
        Integer quantity,
        BigDecimal purchasePrice,
        String status,
        LocalDateTime createdAt
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getProduct().getId(),
                order.getQuantity(),
                order.getPurchasePrice(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}



package com.sumit.flashsale.flash_sale_storefront.dto;

import com.sumit.flashsale.flash_sale_storefront.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponseDto(
        Long id,
        String sku,
        String name,
        String category,
        BigDecimal price,
        Integer stock,
        Boolean flashSale,
        LocalDateTime updatedAt
) {
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getFlashSale(),
                product.getUpdatedAt()
        );
    }
}

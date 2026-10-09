package com.sumit.flashsale.flash_sale_storefront.dto;

import java.math.BigDecimal;

public record WarehouseBatchOrderDto(
        Long orderId,
        Long productId,
        String sku,
        Integer quantity,
        BigDecimal purchasePrice
) {}

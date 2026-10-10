package com.sumit.flashsale.flash_sale_storefront.dto;

import java.math.BigDecimal;

public record WarehouseInventoryDto(
        Long productId,
        String sku,
        Integer physicalStock,
        BigDecimal price,
        Boolean flashSale
) {}

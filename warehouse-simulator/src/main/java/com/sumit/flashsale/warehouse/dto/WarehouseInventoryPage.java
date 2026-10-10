package com.sumit.flashsale.warehouse.dto;

import java.util.List;

public record WarehouseInventoryPage(
        List<WarehouseInventoryItem> content,
        int totalPages,
        long totalElements
) {
    public record WarehouseInventoryItem(
            Long productId,
            String sku,
            Integer physicalStock,
            java.math.BigDecimal price,
            Boolean flashSale
    ) {}
}


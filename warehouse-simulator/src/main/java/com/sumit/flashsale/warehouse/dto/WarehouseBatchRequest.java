package com.sumit.flashsale.warehouse.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WarehouseBatchRequest(
        UUID batchId,
        List<WarehouseOrder> orders
) {

    public record WarehouseOrder(
            Long orderId,
            Long productId,
            String sku,
            Integer quantity,
            BigDecimal purchasePrice
    ) {
    }
}

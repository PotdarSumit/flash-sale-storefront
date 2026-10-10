package com.sumit.flashsale.flash_sale_storefront.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WarehouseInventoryUpdateRequest(
        @NotNull Long productId,
        @NotNull @Min(0) Integer physicalStock,
        @NotNull @DecimalMin("0.0") BigDecimal price
) {
}

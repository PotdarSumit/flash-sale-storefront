package com.sumit.flashsale.flash_sale_storefront.dto;

import java.util.List;
import java.util.UUID;

public record WarehouseBatchResponseDto(
        UUID batchId,
        List<WarehouseBatchOrderDto> orders
) {}

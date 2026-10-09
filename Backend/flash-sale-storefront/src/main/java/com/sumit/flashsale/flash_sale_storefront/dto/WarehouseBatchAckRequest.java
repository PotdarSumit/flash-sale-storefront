package com.sumit.flashsale.flash_sale_storefront.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WarehouseBatchAckRequest(
        @NotNull UUID batchId
) {}

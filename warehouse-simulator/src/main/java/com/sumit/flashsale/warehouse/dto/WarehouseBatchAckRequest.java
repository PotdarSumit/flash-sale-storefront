package com.sumit.flashsale.warehouse.dto;

import java.util.UUID;

public record WarehouseBatchAckRequest(
        UUID batchId
) {
}

package com.sumit.flashsale.flash_sale_storefront.controller;

import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseBatchAckRequest;
import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseBatchResponseDto;
import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseInventoryUpdateRequest;
import com.sumit.flashsale.flash_sale_storefront.service.WarehouseBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/warehouse")
@RequiredArgsConstructor
public class WarehouseBatchController {

    private final WarehouseBatchService warehouseBatchService;

    @PostMapping("/batches/next")
    public ResponseEntity<WarehouseBatchResponseDto> getNextBatch(
            @RequestParam(defaultValue = "100") int batchSize) {

        WarehouseBatchResponseDto batch =
                warehouseBatchService.createBatch(batchSize);

        if (batch == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(batch);
    }


    @PostMapping("/batches/ack")
    public ResponseEntity<Void> acknowledgeBatch(
            @Valid @RequestBody WarehouseBatchAckRequest request) {

        warehouseBatchService.acknowledgeBatch(request);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/inventory")
    public ResponseEntity<Void> updateInventory(
            @Valid @RequestBody WarehouseInventoryUpdateRequest request) {

        warehouseBatchService.updateWarehouseInventory(request);
        return ResponseEntity.noContent().build();
    }

}

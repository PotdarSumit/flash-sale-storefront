package com.sumit.flashsale.warehouse.controller;

import com.sumit.flashsale.warehouse.model.WarehouseInventory;
import com.sumit.flashsale.warehouse.service.WarehouseInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.sumit.flashsale.warehouse.dto.WarehouseBatchRequest;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseInventoryService inventoryService;

    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseInventory addInventory(
            @RequestBody WarehouseInventory inventory) {
        inventoryService.addInventory(inventory);
        return inventory;
    }

    @GetMapping("/inventory")
    public Collection<WarehouseInventory> getAllInventory() {
        return inventoryService.getAllInventory().values();
    }

    @GetMapping("/inventory/{productId}")
    public WarehouseInventory getInventory(
            @PathVariable Long productId) {
        WarehouseInventory inventory =
                inventoryService.getInventory(productId);

        if (inventory == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found");
        }

        return inventory;
    }

    @PatchMapping("/inventory/{productId}/price")
    public WarehouseInventory updatePrice(
            @PathVariable Long productId,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal newPrice = request.get("price");

        if (newPrice == null || newPrice.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A valid price is required");
        }

        inventoryService.updatePrice(productId, newPrice);
        return inventoryService.getInventory(productId);
    }

    @PostMapping("/batches")
    public ResponseEntity<Void> processBatch(
            @RequestBody WarehouseBatchRequest request) {

        inventoryService.processBatch(request);

        return ResponseEntity.noContent().build();
    }
}

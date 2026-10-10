package com.sumit.flashsale.warehouse.service;

import com.sumit.flashsale.warehouse.model.WarehouseInventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.sumit.flashsale.warehouse.dto.WarehouseBatchRequest;
import com.sumit.flashsale.warehouse.dto.WarehouseInventoryPage;

@Service
public class WarehouseInventoryService {

    private final Map<Long, WarehouseInventory> inventory =
            new ConcurrentHashMap<>();

    private final Set<UUID> processedBatches =
            ConcurrentHashMap.newKeySet();

    public void addInventory(WarehouseInventory item) {
        if (item.getPhysicalStock() < 0) {
            throw new IllegalArgumentException(
                    "Physical stock cannot be negative");
        }

        inventory.put(item.getProductId(), item);
    }

    public WarehouseInventory getInventory(Long productId) {
        return inventory.get(productId);
    }

    public int getPhysicalStock(Long productId) {
        WarehouseInventory item = inventory.get(productId);
        return item == null ? 0 : item.getPhysicalStock();
    }


    public synchronized void processBatch(WarehouseBatchRequest request) {

        if (request == null
                || request.batchId() == null
                || request.orders() == null) {
            throw new IllegalArgumentException(
                    "Batch ID and orders are required");
        }

        // Idempotency: don't deduct stock for the same batch twice.
        if (processedBatches.contains(request.batchId())) {
            return;
        }

        // Step 1: Validate individual orders and aggregate quantities.
        Map<Long, Integer> totalQuantityByProduct = new HashMap<>();
        Set<Long> orderIds = new HashSet<>();

        for (WarehouseBatchRequest.WarehouseOrder order : request.orders()) {

            if (order == null
                    || order.orderId() == null
                    || order.productId() == null
                    || order.quantity() == null
                    || order.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid order details");
            }

            if (!orderIds.add(order.orderId())) {
                throw new IllegalArgumentException(
                        "Duplicate order ID: " + order.orderId());
            }

            int previousQuantity = totalQuantityByProduct.getOrDefault(
                    order.productId(), 0);

            int totalQuantity;

            try {
                totalQuantity = Math.addExact(
                        previousQuantity, order.quantity());
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "Total quantity overflow for product "
                                + order.productId());
            }

            totalQuantityByProduct.put(
                    order.productId(), totalQuantity);
        }

        // Step 2: Validate total demand for every product.
        for (Map.Entry<Long, Integer> entry
                : totalQuantityByProduct.entrySet()) {

            Long productId = entry.getKey();
            int requiredQuantity = entry.getValue();

            WarehouseInventory item = inventory.get(productId);

            if (item == null) {
                throw new IllegalArgumentException(
                        "Warehouse product not found: " + productId);
            }

            if (item.getPhysicalStock() < requiredQuantity) {
                throw new IllegalStateException(
                        "Insufficient physical stock for product "
                                + productId
                                + ". Available: " + item.getPhysicalStock()
                                + ", requested: " + requiredQuantity);
            }
        }

        // Step 3: Deduct only after every product passes validation.
        for (Map.Entry<Long, Integer> entry
                : totalQuantityByProduct.entrySet()) {

            WarehouseInventory item = inventory.get(entry.getKey());

            item.setPhysicalStock(
                    item.getPhysicalStock() - entry.getValue());
        }

        // Step 4: Mark the batch processed only after successful deduction.
        processedBatches.add(request.batchId());
    }


    public void updatePrice(Long productId, BigDecimal newPrice) {
        WarehouseInventory item = inventory.get(productId);

        if (item == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative");
        }

        item.setPrice(newPrice);
    }

    public Map<Long, WarehouseInventory> getAllInventory() {
        return Map.copyOf(inventory);
    }


    public void loadInventoryPage(
            List<WarehouseInventoryPage.WarehouseInventoryItem> items) {

        for (var item : items) {
            WarehouseInventory warehouseItem = new WarehouseInventory(
                    item.productId(),
                    item.sku(),
                    item.physicalStock(),
                    item.price(),
                    Boolean.TRUE.equals(item.flashSale())
            );

            addInventory(warehouseItem);
        }
    }

}
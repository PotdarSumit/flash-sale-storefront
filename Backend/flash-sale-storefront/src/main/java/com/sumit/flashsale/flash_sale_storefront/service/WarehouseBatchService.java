package com.sumit.flashsale.flash_sale_storefront.service;

import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseBatchAckRequest;
import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseBatchOrderDto;
import com.sumit.flashsale.flash_sale_storefront.dto.WarehouseBatchResponseDto;
import com.sumit.flashsale.flash_sale_storefront.entity.Order;
import com.sumit.flashsale.flash_sale_storefront.entity.WarehouseBatch;
import com.sumit.flashsale.flash_sale_storefront.repository.OrderRepository;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import com.sumit.flashsale.flash_sale_storefront.repository.WarehouseBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseBatchService {

    private final OrderRepository orderRepository;
    private final WarehouseBatchRepository warehouseBatchRepository;
    private final ProductRepository productRepository;

    @Transactional
    public WarehouseBatchResponseDto createBatch(int batchSize) {

        // Validate requested batch size.
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException(
                    "Batch size must be between 1 and 1000"
            );
        }

        // Reuse an existing unacknowledged batch on retry.
        var existingBatch =
                warehouseBatchRepository
                        .findFirstByStatusOrderByCreatedAtAsc("CREATED");

        if (existingBatch.isPresent()) {
            WarehouseBatch batch = existingBatch.get();

            List<Order> existingOrders =
                    orderRepository
                            .findByWarehouseBatchIdAndWarehouseSyncedFalse(
                                    batch.getBatchId()
                            );

            List<WarehouseBatchOrderDto> items =
                    existingOrders.stream()
                            .map(this::toBatchOrderDto)
                            .toList();

            return new WarehouseBatchResponseDto(
                    batch.getBatchId(),
                    items
            );
        }

        // Select unassigned orders while locking their database rows.
        List<Order> orders =
                orderRepository.findOrdersForWarehouseBatch(batchSize);

        if (orders.isEmpty()) {
            return null;
        }

        // Create a new batch with a generated UUID.
        WarehouseBatch batch = new WarehouseBatch();
        batch.setStatus("CREATED");

        batch = warehouseBatchRepository.save(batch);

        // Assign all selected orders to this batch.
        for (Order order : orders) {
            order.setWarehouseBatchId(batch.getBatchId());
        }

        orderRepository.saveAll(orders);

        // Build the response sent to the warehouse.
        List<WarehouseBatchOrderDto> items =
                orders.stream()
                        .map(this::toBatchOrderDto)
                        .toList();

        return new WarehouseBatchResponseDto(
                batch.getBatchId(),
                items
        );
    }


    @Transactional
    public void acknowledgeBatch(WarehouseBatchAckRequest request) {

        WarehouseBatch batch = warehouseBatchRepository
                .findByBatchIdForUpdate(request.batchId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Warehouse batch not found")
                );

        if ("ACKNOWLEDGED".equals(batch.getStatus())) {
            return;
        }


        List<Order> orders =
                orderRepository.findByWarehouseBatchIdAndWarehouseSyncedFalse(
                        batch.getBatchId()
                );


        Map<Long, Integer> quantitiesByProduct = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getProduct().getId(),
                        Collectors.summingInt(Order::getQuantity)
                ));


        for (Map.Entry<Long, Integer> entry :
                quantitiesByProduct.entrySet()) {

            int updated = productRepository.acknowledgeWarehouseQuantity(
                    entry.getKey(),
                    entry.getValue()
            );

            if (updated != 1) {
                throw new IllegalStateException(
                        "Warehouse inventory mismatch for product "
                                + entry.getKey()
                );
            }
        }


        orders.forEach(order -> order.setWarehouseSynced(true));
        orderRepository.saveAll(orders);

        batch.setStatus("ACKNOWLEDGED");
        batch.setAcknowledgedAt(LocalDateTime.now());
        warehouseBatchRepository.save(batch);
    }

    private WarehouseBatchOrderDto toBatchOrderDto(Order order) {
        return new WarehouseBatchOrderDto(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getSku(),
                order.getQuantity(),
                order.getPurchasePrice()
        );
    }


    @Transactional
    public void updateWarehouseInventory(
            com.sumit.flashsale.flash_sale_storefront.dto.WarehouseInventoryUpdateRequest request) {

        if (request == null
                || request.productId() == null
                || request.physicalStock() == null
                || request.physicalStock() < 0
                || request.price() == null
                || request.price().signum() < 0) {
            throw new IllegalArgumentException("Invalid warehouse inventory update");
        }

        int updated = productRepository.updateWarehouseInventory(
                request.productId(),
                request.physicalStock(),
                request.price()
        );

        if (updated != 1) {
            throw new IllegalArgumentException(
                    "Product not found: " + request.productId()
            );
        }
    }

}
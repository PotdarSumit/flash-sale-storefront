package com.sumit.flashsale.flash_sale_storefront.repository;

import com.sumit.flashsale.flash_sale_storefront.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdentifierOrderByCreatedAtDesc(
            String customerIdentifier
    );
    boolean existsByProductIdAndCustomerIdentifierAndStatusIn(
            Long productId,
            String customerIdentifier,
            List<com.sumit.flashsale.flash_sale_storefront.entity.OrderStatus> statuses
    );
    List<Order> findByWarehouseBatchIdAndWarehouseSyncedFalse(
            UUID warehouseBatchId
    );

    @Query(value = """
    SELECT *
    FROM orders
    WHERE warehouse_synced = FALSE
      AND warehouse_batch_id IS NULL
    ORDER BY created_at, id
    LIMIT :batchSize
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<Order> findOrdersForWarehouseBatch(
            @Param("batchSize") int batchSize
    );
}

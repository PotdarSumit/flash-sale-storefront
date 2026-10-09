package com.sumit.flashsale.flash_sale_storefront.repository;

import com.sumit.flashsale.flash_sale_storefront.entity.WarehouseBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseBatchRepository
        extends JpaRepository<WarehouseBatch, UUID> {
    Optional<WarehouseBatch> findFirstByStatusOrderByCreatedAtAsc(
            String status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT b FROM WarehouseBatch b
    WHERE b.batchId = :batchId
    """)
    Optional<WarehouseBatch> findByBatchIdForUpdate(
            @Param("batchId") UUID batchId
    );
}

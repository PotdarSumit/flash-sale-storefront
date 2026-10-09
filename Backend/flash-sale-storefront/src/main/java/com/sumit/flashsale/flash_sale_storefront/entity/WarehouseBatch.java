package com.sumit.flashsale.flash_sale_storefront.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "warehouse_batches")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseBatch {

    @Id
    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(nullable = false, length = 20)
    private String status = "CREATED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @PrePersist
    protected void onCreate() {
        if (batchId == null) {
            batchId = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

package com.sumit.flashsale.flash_sale_storefront.repository;

import com.sumit.flashsale.flash_sale_storefront.entity.WarehouseUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseUpdateRepository
        extends JpaRepository<WarehouseUpdate, Long> {

    List<WarehouseUpdate> findByProductIdOrderByReceivedAtDesc(
            Long productId
    );
}

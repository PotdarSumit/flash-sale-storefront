package com.sumit.flashsale.flash_sale_storefront.repository;

import com.sumit.flashsale.flash_sale_storefront.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;


import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name,
            String sku,
            Pageable pageable
    );

    Page<Product> findByCategoryIgnoreCase(
            String category,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findDistinctCategories();
    List<Product> findByFlashSaleTrue();


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
    UPDATE Product p
    SET p.stock = p.stock - :quantity,
        p.pendingWarehouseQuantity =
            p.pendingWarehouseQuantity + :quantity,
        p.updatedAt = CURRENT_TIMESTAMP
    WHERE p.id = :productId
      AND p.stock >= :quantity
    """)
    int reserveStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
    UPDATE Product p
    SET p.warehouseStock = p.warehouseStock - :quantity,
        p.pendingWarehouseQuantity =
            p.pendingWarehouseQuantity - :quantity
    WHERE p.id = :productId
      AND p.warehouseStock >= :quantity
      AND p.pendingWarehouseQuantity >= :quantity
    """)
    int acknowledgeWarehouseQuantity(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(
            @Param("productId") Long productId
    );
}

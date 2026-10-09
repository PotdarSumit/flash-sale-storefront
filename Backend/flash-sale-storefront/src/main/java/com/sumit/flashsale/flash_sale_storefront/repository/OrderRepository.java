package com.sumit.flashsale.flash_sale_storefront.repository;

import com.sumit.flashsale.flash_sale_storefront.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdentifierOrderByCreatedAtDesc(
            String customerIdentifier
    );
    boolean existsByProductIdAndCustomerIdentifierAndStatusIn(
            Long productId,
            String customerIdentifier,
            List<com.sumit.flashsale.flash_sale_storefront.entity.OrderStatus> statuses
    );
}

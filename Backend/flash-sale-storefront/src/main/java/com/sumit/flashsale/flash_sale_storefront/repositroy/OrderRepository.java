package com.sumit.flashsale.flash_sale_storefront.repositroy;

import com.sumit.flashsale.flash_sale_storefront.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdentifierOrderByCreatedAtDesc(
            String customerIdentifier
    );

    List<Order> findByStatus(String status);
}

package com.sumit.flashsale.flash_sale_storefront.repositroy;

import com.sumit.flashsale.flash_sale_storefront.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByFlashSaleTrue();
}

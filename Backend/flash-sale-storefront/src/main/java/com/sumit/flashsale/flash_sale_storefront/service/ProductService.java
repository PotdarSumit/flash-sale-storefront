package com.sumit.flashsale.flash_sale_storefront.service;

import com.sumit.flashsale.flash_sale_storefront.entity.Product;
import com.sumit.flashsale.flash_sale_storefront.repositroy.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getFlashSaleProducts() {
        return productRepository.findByFlashSaleTrue();
    }
}


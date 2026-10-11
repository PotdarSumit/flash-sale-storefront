package com.sumit.flashsale.flash_sale_storefront.service;

import com.sumit.flashsale.flash_sale_storefront.dto.ProductResponseDto;
import com.sumit.flashsale.flash_sale_storefront.entity.Product;
import com.sumit.flashsale.flash_sale_storefront.exception.ProductNotFoundException;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final DatabaseReadLimiter databaseReadLimiter;
    private final MeterRegistry meterRegistry;

    public ProductService(
            ProductRepository productRepository,
            DatabaseReadLimiter databaseReadLimiter,
            MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.databaseReadLimiter = databaseReadLimiter;
        this.meterRegistry = meterRegistry;
    }


    private void recordDatabaseRead() {
        meterRegistry.counter("storefront.database.product.reads").increment();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(int page, int size) {
        databaseReadLimiter.acquirePermit();
        recordDatabaseRead();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );

        return productRepository.findAll(pageable)
                .map(ProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id", sync = true)
    public ProductResponseDto getProductById(Long id) {
        databaseReadLimiter.acquirePermit();
        recordDatabaseRead();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductResponseDto.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(
            String keyword,
            int page,
            int size) {

        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException(
                    "Search keyword must not be blank"
            );
        }

        databaseReadLimiter.acquirePermit();
        recordDatabaseRead();

        String searchTerm = keyword.trim();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );

        return productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                        searchTerm,
                        searchTerm,
                        pageable
                )
                .map(ProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<String> getCategories() {
        databaseReadLimiter.acquirePermit();
        recordDatabaseRead();

        return productRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "flashSaleProducts", key = "'all'")
    public List<ProductResponseDto> getFlashSaleProducts() {
        databaseReadLimiter.acquirePermit();
        recordDatabaseRead();

        return productRepository.findByFlashSaleTrue()
                .stream()
                .map(ProductResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(
            String category,
            int page,
            int size) {

        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException(
                    "Category must not be blank"
            );
        }

        databaseReadLimiter.acquirePermit();
        recordDatabaseRead();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );

        return productRepository
                .findByCategoryIgnoreCase(category.trim(), pageable)
                .map(ProductResponseDto::from);
    }
}

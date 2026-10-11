package com.sumit.flashsale.flash_sale_storefront.service;

import com.sumit.flashsale.flash_sale_storefront.dto.OrderResponseDto;
import com.sumit.flashsale.flash_sale_storefront.dto.PurchaseRequestDto;
import com.sumit.flashsale.flash_sale_storefront.entity.Order;
import com.sumit.flashsale.flash_sale_storefront.entity.OrderStatus;
import com.sumit.flashsale.flash_sale_storefront.entity.Product;
import com.sumit.flashsale.flash_sale_storefront.repository.OrderRepository;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CacheManager cacheManager) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public OrderResponseDto placeOrder(PurchaseRequestDto request) {

        // Lock the product row while validating and reserving stock.
        Product product = productRepository
                .findByIdForUpdate(request.productId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        int quantity = request.quantity();

        // Normal order limit.
        if (quantity > 5) {
            throw new IllegalArgumentException(
                    "You can purchase at most 5 units per order");
        }

        // Flash-sale order limit.
        if (Boolean.TRUE.equals(product.getFlashSale()) && quantity != 1) {
            throw new IllegalArgumentException(
                    "Flash-sale products can only be purchased one unit at a time");
        }

        // Prevent repeat flash-sale purchases by the same customer.
        if (Boolean.TRUE.equals(product.getFlashSale())) {
            boolean alreadyPurchased =
                    orderRepository
                            .existsByProductIdAndCustomerIdentifierAndStatusIn(
                                    product.getId(),
                                    request.customerIdentifier(),
                                    List.of(
                                            OrderStatus.PENDING,
                                            OrderStatus.CONFIRMED
                                    )
                            );

            if (alreadyPurchased) {
                throw new IllegalArgumentException(
                        "You have already purchased this flash-sale product");
            }
        }

        // Reject a price mismatch instead of charging a different price.
        if (product.getPrice().compareTo(request.displayedPrice()) != 0) {
            throw new IllegalArgumentException(
                    "The product price has changed. Please refresh and try again");
        }

        // Atomically reserve stock.
        int updatedRows = productRepository.reserveStock(
                product.getId(),
                quantity
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock available");
        }

        // Save the order in the same transaction.
        Order order = new Order();
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setPurchasePrice(request.displayedPrice());
        order.setCustomerIdentifier(request.customerIdentifier());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        Long productId = product.getId();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var cache = cacheManager.getCache("products");
                        if (cache != null) {
                            cache.evict(productId);
                        }
                    }
                }
        );

        return OrderResponseDto.from(savedOrder);
    }
}



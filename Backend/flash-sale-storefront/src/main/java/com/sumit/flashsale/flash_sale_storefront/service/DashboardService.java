package com.sumit.flashsale.flash_sale_storefront.service;



import com.sumit.flashsale.flash_sale_storefront.dto.DashboardMetricsDto;
import com.sumit.flashsale.flash_sale_storefront.entity.OrderStatus;
import com.sumit.flashsale.flash_sale_storefront.repository.OrderRepository;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.sumit.flashsale.flash_sale_storefront.service.DatabaseReadLimiter;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DatabaseReadLimiter databaseReadLimiter;

    public DashboardService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            DatabaseReadLimiter databaseReadLimiter) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.databaseReadLimiter = databaseReadLimiter;
    }

    public DashboardMetricsDto getMetrics() {
        return new DashboardMetricsDto(
                productRepository.count(),
                productRepository.countByFlashSaleTrue(),
                productRepository.countByStockLessThanEqual(0),
                productRepository.countByPendingWarehouseQuantityGreaterThan(0),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByWarehouseSyncedFalseAndWarehouseBatchIdIsNull(),
                databaseReadLimiter.getAllowedCount(),
                databaseReadLimiter.getRejectedCount()
        );
    }
}


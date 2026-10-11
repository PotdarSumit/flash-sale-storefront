package com.sumit.flashsale.flash_sale_storefront.service;




import com.sumit.flashsale.flash_sale_storefront.dto.DashboardMetricsDto;
import com.sumit.flashsale.flash_sale_storefront.entity.OrderStatus;
import com.sumit.flashsale.flash_sale_storefront.repository.OrderRepository;
import com.sumit.flashsale.flash_sale_storefront.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final String DATABASE_READ_METRIC =
            "storefront.database.product.reads";

    private static final double MAX_DATABASE_READS_PER_SECOND = 25.0;

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DatabaseReadLimiter databaseReadLimiter;
    private final MeterRegistry meterRegistry;

    private double previousReadCount = -1;
    private long previousSampleTimeNanos = 0;
    private double databaseProductReadsPerSecond = 0;

    public DashboardService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            DatabaseReadLimiter databaseReadLimiter,
            MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.databaseReadLimiter = databaseReadLimiter;
        this.meterRegistry = meterRegistry;
    }

    public synchronized DashboardMetricsDto getMetrics() {
        boolean hasPreviousSample = previousSampleTimeNanos > 0;
        updateDatabaseReadRate();

        String r3Status;

        if (!hasPreviousSample) {
            r3Status = "COLLECTING_BASELINE";
        } else if (databaseProductReadsPerSecond
                <= MAX_DATABASE_READS_PER_SECOND) {
            r3Status = "WITHIN_LIMIT_IN_LAST_SAMPLE";
        } else {
            r3Status = "LIMIT_EXCEEDED";
        }

        return new DashboardMetricsDto(
                productRepository.count(),
                productRepository.countByFlashSaleTrue(),
                productRepository.countByStockLessThanEqual(0),
                productRepository.countByPendingWarehouseQuantityGreaterThan(0),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByWarehouseSyncedFalseAndWarehouseBatchIdIsNull(),
                databaseReadLimiter.getAllowedCount(),
                databaseReadLimiter.getRejectedCount(),
                databaseProductReadsPerSecond,
                r3Status
        );
    }

    private void updateDatabaseReadRate() {
        Counter counter = meterRegistry.find(DATABASE_READ_METRIC).counter();

        double currentCount = counter == null ? 0 : counter.count();
        long currentTimeNanos = System.nanoTime();

        if (previousReadCount >= 0 && previousSampleTimeNanos > 0) {
            double elapsedSeconds =
                    (currentTimeNanos - previousSampleTimeNanos) / 1_000_000_000.0;

            if (elapsedSeconds > 0) {
                double countDifference = currentCount - previousReadCount;

                // A counter can reset after an application restart.
                databaseProductReadsPerSecond = countDifference >= 0
                        ? countDifference / elapsedSeconds
                        : 0;
            }
        }

        previousReadCount = currentCount;
        previousSampleTimeNanos = currentTimeNanos;
    }
}

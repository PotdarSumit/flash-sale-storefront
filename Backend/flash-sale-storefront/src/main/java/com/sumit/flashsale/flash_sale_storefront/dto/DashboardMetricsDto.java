package com.sumit.flashsale.flash_sale_storefront.dto;



public record DashboardMetricsDto(
        long totalProducts,
        long flashSaleProducts,
        long soldOutProducts,
        long productsWithPendingWarehouseQuantity,
        long pendingOrders,
        long unsyncedOrders,
        long databaseReadPermitsGranted,
        long databaseReadRequestsRejected
) {
}


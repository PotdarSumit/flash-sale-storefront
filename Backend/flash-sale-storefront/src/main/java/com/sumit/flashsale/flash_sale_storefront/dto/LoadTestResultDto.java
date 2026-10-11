package com.sumit.flashsale.flash_sale_storefront.dto;

public record LoadTestResultDto(
        String status,
        String message,
        int durationSeconds,
        double requestsPerSecond,
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        double p95LatencyMs,
        double databaseReadsPerSecond,
        Boolean r3Passed,
        Boolean r4Passed,
        Boolean r5Passed
) {
}


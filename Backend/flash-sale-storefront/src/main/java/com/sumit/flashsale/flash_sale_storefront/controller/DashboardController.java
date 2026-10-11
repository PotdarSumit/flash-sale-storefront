package com.sumit.flashsale.flash_sale_storefront.controller;

import com.sumit.flashsale.flash_sale_storefront.dto.DashboardMetricsDto;
import com.sumit.flashsale.flash_sale_storefront.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    public DashboardMetricsDto getMetrics() {
        return dashboardService.getMetrics();
    }
}


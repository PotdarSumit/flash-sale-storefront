package com.sumit.flashsale.flash_sale_storefront.controller;


import com.sumit.flashsale.flash_sale_storefront.dto.DashboardMetricsDto;
import com.sumit.flashsale.flash_sale_storefront.dto.LoadTestResultDto;
import com.sumit.flashsale.flash_sale_storefront.service.DashboardLoadTestService;
import com.sumit.flashsale.flash_sale_storefront.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardLoadTestService loadTestService;

    public DashboardController(
            DashboardService dashboardService,
            DashboardLoadTestService loadTestService) {
        this.dashboardService = dashboardService;
        this.loadTestService = loadTestService;
    }

    @GetMapping("/metrics")
    public DashboardMetricsDto getMetrics() {
        return dashboardService.getMetrics();
    }

    @PostMapping("/load-test")
    public LoadTestResultDto runLoadTest(
            @RequestBody(required = false) LoadTestRequest request) {
        int duration = request == null ? 120 : request.durationSeconds();
        return loadTestService.runReadTest(duration);
    }

    public record LoadTestRequest(int durationSeconds) {
    }
}



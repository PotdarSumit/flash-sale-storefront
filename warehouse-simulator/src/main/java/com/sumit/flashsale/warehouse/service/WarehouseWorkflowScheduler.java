package com.sumit.flashsale.warehouse.service;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseWorkflowScheduler {

    private final WarehouseWorkflowService workflowService;

    @Scheduled(
            fixedDelay = 30_000,
            initialDelay = 5_000
    )
    public void processWarehouseBatches() {
        workflowService.processNextBatch();
    }
}


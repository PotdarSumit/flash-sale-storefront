package com.sumit.flashsale.warehouse.service;

import com.sumit.flashsale.warehouse.dto.WarehouseInventoryPage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WarehouseInventoryLoader {

    private static final Logger log =
            LoggerFactory.getLogger(WarehouseInventoryLoader.class);

    private final RestClient storefrontRestClient;
    private final WarehouseInventoryService inventoryService;

    @Scheduled(initialDelay = 2_000, fixedDelay = 30_000)
    public void loadInventory() {
        if (!inventoryService.getAllInventory().isEmpty()) {
            return;
        }

        try {
            int page = 0;
            int totalPages;

            do {
                WarehouseInventoryPage result = storefrontRestClient.get()
                        .uri("/api/internal/warehouse/inventory?page={page}&size=500",
                                page)
                        .retrieve()
                        .body(WarehouseInventoryPage.class);

                if (result == null || result.content() == null) {
                    log.warn("Storefront returned an empty inventory response");
                    return;
                }

                inventoryService.loadInventoryPage(result.content());
                totalPages = result.totalPages();

                log.info("Loaded inventory page {}/{}",
                        page + 1, totalPages);

                page++;

                // Keep inventory snapshot requests below 25 per second.
                Thread.sleep(50);

            } while (page < totalPages);

            log.info("Warehouse inventory initialized: {} products",
                    inventoryService.getAllInventory().size());

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Inventory loading was interrupted");
        } catch (Exception exception) {
            log.error("Could not load inventory from storefront: {}",
                    exception.getMessage());
        }
    }
}


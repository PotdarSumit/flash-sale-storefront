package com.sumit.flashsale.warehouse.service;


import com.sumit.flashsale.warehouse.dto.WarehouseBatchAckRequest;
import com.sumit.flashsale.warehouse.dto.WarehouseBatchRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;



@Service
@RequiredArgsConstructor
public class WarehouseWorkflowService {

    private static final Logger log =
            LoggerFactory.getLogger(WarehouseWorkflowService.class);

    private final RestClient storefrontRestClient;
    private final WarehouseInventoryService inventoryService;

    public void processNextBatch() {
        WarehouseBatchRequest batch;

        try {
            batch = storefrontRestClient.post()
                    .uri("/api/internal/warehouse/batches/next?batchSize=100")
                    .retrieve()
                    .onStatus(
                            status -> status.value() == HttpStatus.NO_CONTENT.value(),
                            (request, response) -> {
                                // No pending orders is a normal condition.
                            }
                    )
                    .body(WarehouseBatchRequest.class);
        } catch (RestClientResponseException exception) {
            log.error("Could not fetch warehouse batch: HTTP {}",
                    exception.getStatusCode().value());
            return;
        } catch (Exception exception) {
            log.error("Could not connect to storefront", exception);
            return;
        }

        if (batch == null || batch.orders() == null || batch.orders().isEmpty()) {
            log.debug("No pending warehouse orders");
            return;
        }

        try {
            inventoryService.processBatch(batch);

            storefrontRestClient.post()
                    .uri("/api/internal/warehouse/batches/ack")
                    .body(new WarehouseBatchAckRequest(batch.batchId()))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully processed and acknowledged batch {}",
                    batch.batchId());

        } catch (Exception exception) {
            log.error("Warehouse batch {} failed; acknowledgement not completed",
                    batch.batchId(), exception);
        }
    }
}


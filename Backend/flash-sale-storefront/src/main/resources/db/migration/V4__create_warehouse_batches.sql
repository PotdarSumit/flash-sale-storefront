
-- Track which warehouse batch contains each order.
ALTER TABLE orders
    ADD COLUMN warehouse_batch_id UUID,
    ADD COLUMN warehouse_synced BOOLEAN NOT NULL DEFAULT FALSE;

-- Store each batch and its processing status.
CREATE TABLE warehouse_batches (
    batch_id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP NULL,

    CONSTRAINT chk_warehouse_batch_status
        CHECK (status IN ('CREATED', 'ACKNOWLEDGED'))
);

-- Connect orders to their assigned batches.
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_warehouse_batch
    FOREIGN KEY (warehouse_batch_id)
    REFERENCES warehouse_batches(batch_id);

-- Find orders waiting to be assigned to a batch.
CREATE INDEX idx_orders_warehouse_pending
    ON orders (created_at)
    WHERE warehouse_synced = FALSE;
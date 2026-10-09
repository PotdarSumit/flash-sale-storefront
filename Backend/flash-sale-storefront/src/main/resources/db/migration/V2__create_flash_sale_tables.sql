
-- V2__create_flash_sale_tables.sql

-- 1. Products
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    is_flash_sale BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_products_price_non_negative
        CHECK (price >= 0),

    CONSTRAINT chk_products_stock_non_negative
        CHECK (stock >= 0)
);

-- 2. Orders
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    purchase_price NUMERIC(10, 2) NOT NULL,
    customer_identifier VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_orders_quantity_positive
        CHECK (quantity > 0),

    CONSTRAINT chk_orders_purchase_price_non_negative
        CHECK (purchase_price >= 0),

    CONSTRAINT chk_orders_status
        CHECK (status IN (
            'PENDING',
            'CONFIRMED',
            'FAILED',
            'CANCELLED'
        ))
);

-- 3. Warehouse updates
CREATE TABLE warehouse_updates (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    stock INTEGER NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_warehouse_updates_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_warehouse_stock_non_negative
        CHECK (stock >= 0),

    CONSTRAINT chk_warehouse_price_non_negative
        CHECK (price >= 0)
);

-- Indexes for common query patterns

-- Orders filtered by product and sorted by creation time
CREATE INDEX idx_orders_product_created_at
    ON orders (product_id, created_at DESC);

-- Customer order history
CREATE INDEX idx_orders_customer_created_at
    ON orders (customer_identifier, created_at DESC);

-- Orders filtered by status
CREATE INDEX idx_orders_status
    ON orders (status);

-- Warehouse update history for a product
CREATE INDEX idx_warehouse_updates_product_received_at
    ON warehouse_updates (product_id, received_at DESC);

-- Flash sale product listings
CREATE INDEX idx_products_flash_sale
    ON products (id)
    WHERE is_flash_sale = TRUE;
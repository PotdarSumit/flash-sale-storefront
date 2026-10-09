ALTER TABLE products
    ADD COLUMN warehouse_stock INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN pending_warehouse_quantity INTEGER NOT NULL DEFAULT 0;


UPDATE products
SET warehouse_stock = stock,
    pending_warehouse_quantity = 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_warehouse_stock_nonnegative
        CHECK (warehouse_stock >= 0),
    ADD CONSTRAINT chk_products_pending_warehouse_nonnegative
        CHECK (pending_warehouse_quantity >= 0);

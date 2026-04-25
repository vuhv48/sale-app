-- =============================================================================
-- FILE: V10__sales_core.sql
-- PURPOSE (EN):
-- - Add minimal B2B sales core tables for MVP business flows.
-- - Cover customer, product/sku, order, order item and order status history.
-- MUC DICH:
-- - Bo sung bo bang nghiep vu ban hang toi thieu de phat trien use-case thuc te.
-- =============================================================================

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_code   VARCHAR(32)  NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(32),
    email           VARCHAR(255),
    tax_code        VARCHAR(64),
    address_line    VARCHAR(500),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_customers_name ON customers (name);
CREATE INDEX idx_customers_active ON customers (is_active) WHERE is_deleted = false;

CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code    VARCHAR(32)  NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_products_name ON products (name);

CREATE TABLE product_skus (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID          NOT NULL REFERENCES products (id),
    sku_code        VARCHAR(64)   NOT NULL UNIQUE,
    sku_name        VARCHAR(255)  NOT NULL,
    unit_price      NUMERIC(18,2) NOT NULL,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_product_skus_product_id ON product_skus (product_id);

CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no         VARCHAR(32)   NOT NULL UNIQUE,
    customer_id      UUID          NOT NULL REFERENCES customers (id),
    order_status     VARCHAR(32)   NOT NULL,
    order_date       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    note             VARCHAR(1000),
    total_amount     NUMERIC(18,2) NOT NULL DEFAULT 0,
    is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by       VARCHAR(255),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by       VARCHAR(255)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (order_status);
CREATE INDEX idx_orders_date ON orders (order_date DESC);

CREATE TABLE order_items (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id          UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    sku_id            UUID           NOT NULL REFERENCES product_skus (id),
    quantity          NUMERIC(18,3)  NOT NULL,
    unit_price        NUMERIC(18,2)  NOT NULL,
    line_total        NUMERIC(18,2)  NOT NULL,
    is_deleted        BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by        VARCHAR(255),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_sku_id ON order_items (sku_id);

CREATE TABLE order_status_history (
    id               BIGSERIAL PRIMARY KEY,
    order_id         UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    from_status      VARCHAR(32),
    to_status        VARCHAR(32)  NOT NULL,
    changed_reason   VARCHAR(500),
    changed_by       VARCHAR(255),
    changed_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history (order_id, changed_at DESC);



/*
INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES ('CUSTOMER_API_READ', 'CUSTOMER', 'READ', 'Read student API', '/api/customers/**', 'GET')
ON CONFLICT (resource_code) DO NOTHING;


INSERT INTO authz.role_resources (role_id, resource_id)
SELECT r.id, ar.id
FROM authz.roles r
JOIN authz.resources ar ON ar.resource_code IN ('CUSTOMER_API_READ', 'STUDENT_API_CREATE')
WHERE r.code = 'USER'
ON CONFLICT (role_id, resource_id) DO NOTHING;

 */

-- =========================
-- CARTS TABLE
-- =========================
CREATE TABLE carts (
                       id VARCHAR(255) PRIMARY KEY,
                       user_id VARCHAR(255) NOT NULL,
                       checkout_state VARCHAR(50) NOT NULL,
                       checkout_expires_at TIMESTAMP,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP
);

-- One cart per user
ALTER TABLE carts
    ADD CONSTRAINT uk_cart_user_id UNIQUE (user_id);

-- Indexes
CREATE INDEX idx_cart_user_id ON carts (user_id);
CREATE INDEX idx_cart_checkout_state ON carts (checkout_state);


-- =========================
-- CART ITEMS TABLE
-- =========================
CREATE TABLE cart_items (
                            id VARCHAR(255) PRIMARY KEY,
                            cart_id VARCHAR(255) NOT NULL,
                            product_id VARCHAR(255) NOT NULL,
                            quantity INTEGER NOT NULL,
                            checkout_price_snapshot NUMERIC(19, 2)
);

-- Foreign key relationship (CartItem -> Cart)
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
            REFERENCES carts(id)
            ON DELETE CASCADE;

-- One product per cart constraint
ALTER TABLE cart_items
    ADD CONSTRAINT uk_cart_item_cart_product
        UNIQUE (cart_id, product_id);

-- Index for fast lookup of cart items
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);
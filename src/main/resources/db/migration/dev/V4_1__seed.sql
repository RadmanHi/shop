-- =========================================================
-- DEV SEED DATA (ONLY DEV PROFILE)
-- =========================================================

-- PRODUCTS
INSERT INTO products (id, sku, name, price, total_quantity, reserved_quantity, version, created_at, updated_at)
VALUES
    ('product-1', 'SKU-IPHONE-001', 'iPhone 15 Pro', 1200, 50, 0, 0, now(), now()),
    ('product-2', 'SKU-IPHONE-002', 'iPhone 15', 1000, 60, 0, 0, now(), now()),
    ('product-3', 'SKU-MAC-001', 'MacBook Pro 16', 2500, 30, 0, 0, now(), now()),
    ('product-4', 'SKU-MAC-002', 'MacBook Air', 1500, 40, 0, 0, now(), now()),
    ('product-5', 'SKU-AIRPODS-001', 'AirPods Pro', 250, 100, 0, 0, now(), now()),
    ('product-6', 'SKU-IPAD-001', 'iPad Air', 800, 45, 0, 0, now(), now()),
    ('product-7', 'SKU-APPLEWATCH-001', 'Apple Watch Series 9', 500, 70, 0, 0, now(), now()),
    ('product-8', 'SKU-KEYBOARD-001', 'Magic Keyboard', 120, 120, 0, 0, now(), now()),
    ('product-9', 'SKU-MOUSE-001', 'Magic Mouse', 90, 150, 0, 0, now(), now()),
    ('product-10', 'SKU-DISPLAY-001', 'Studio Display', 1600, 20, 0, 0, now(), now()),
    ('product-11', 'SKU-CHARGER-001', 'USB-C Charger 30W', 25, 300, 0, 0, now(), now()),
    ('product-12', 'SKU-CABLE-001', 'USB-C Cable', 15, 500, 0, 0, now(), now());

-- CARTS
INSERT INTO carts (id, user_id, checkout_state, checkout_expires_at, created_at, updated_at)
VALUES
    ('cart-1', 'user-1', 'IDLE', NULL, now(), now()),
    ('cart-2', 'user-2', 'IDLE', NULL, now(), now()),
    ('cart-3', 'user-3', 'IDLE', NULL, now(), now()),
    ('cart-4', 'user-4', 'CHECKOUT_IN_PROGRESS', now() + interval '10 minutes', now(), now()),
    ('cart-5', 'user-5', 'IDLE', NULL, now(), now());

-- CART ITEMS
INSERT INTO cart_items (
    id,
    cart_id,
    product_id,
    quantity,
    checkout_price_snapshot,
    version,
    created_at,
    updated_at
)
VALUES
    ('item-1', 'cart-1', 'product-1', 1, NULL, 0, now(), now()),
    ('item-2', 'cart-1', 'product-5', 2, NULL, 0, now(), now()),

    ('item-3', 'cart-2', 'product-3', 1, NULL, 0, now(), now()),
    ('item-4', 'cart-2', 'product-6', 1, NULL, 0, now(), now()),
    ('item-5', 'cart-2', 'product-9', 2, NULL, 0, now(), now()),

    ('item-6', 'cart-4', 'product-2', 1, 1000, 1, now(), now()),
    ('item-7', 'cart-4', 'product-5', 2, 250, 1, now(), now()),

    ('item-8', 'cart-5', 'product-11', 5, NULL, 0, now(), now()),
    ('item-9', 'cart-5', 'product-12', 10, NULL, 0, now(), now()),
    ('item-10', 'cart-5', 'product-8', 1, NULL, 0, now(), now());
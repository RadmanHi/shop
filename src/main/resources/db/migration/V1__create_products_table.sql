CREATE TABLE products (
                          id VARCHAR(255) NOT NULL,
                          sku VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,

                          price NUMERIC(19, 2) NOT NULL CHECK (price >= 0),

                          total_quantity INT NOT NULL DEFAULT 0 CHECK (total_quantity >= 0),
                          reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),

                          version BIGINT NOT NULL DEFAULT 0,

                          created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

                          CONSTRAINT pk_products PRIMARY KEY (id)
);

ALTER TABLE products
    ADD CONSTRAINT uk_product_sku UNIQUE (sku);
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    shopping_cart_id UUID,
    username VARCHAR(255) NOT NULL,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR(255) NOT NULL,
    delivery_weight NUMERIC(9,4),
    delivery_volume NUMERIC(9,4),
    fragile BOOLEAN,
    total_price NUMERIC(9,2),
    delivery_price NUMERIC(9,2),
    product_price NUMERIC(9,2)
);

CREATE TABLE IF NOT EXISTS order_items (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_address (
    order_id UUID NOT NULL,
    country VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    house VARCHAR(255) NOT NULL,
    flat VARCHAR(255) NOT NULL,
    PRIMARY KEY (order_id),
    CONSTRAINT fk_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE
);
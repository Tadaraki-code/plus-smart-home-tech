CREATE TABLE IF NOT EXISTS cart (
    id UUID PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_item (
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (cart_id, product_id),
    CONSTRAINT fk_cart
        FOREIGN KEY (cart_id)
        REFERENCES cart (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cart_user_name
    ON cart (user_name);
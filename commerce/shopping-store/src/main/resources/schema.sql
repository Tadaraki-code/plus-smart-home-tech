CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(255),
    quantity_state VARCHAR(50) NOT NULL,
    product_state VARCHAR(50) NOT NULL,
    product_category VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 1)
);
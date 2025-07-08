DROP TABLE IF EXISTS delivery_address;
DROP TABLE IF EXISTS warehouse_address;
DROP TABLE IF EXISTS delivery;

CREATE TABLE IF NOT EXISTS delivery (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    delivery_state VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS delivery_address (
    delivery_id UUID NOT NULL,
    country VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    house VARCHAR(255) NOT NULL,
    flat VARCHAR(255) NOT NULL,
    PRIMARY KEY (delivery_id),
    CONSTRAINT delivery_id
        FOREIGN KEY (delivery_id)
        REFERENCES delivery (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS warehouse_address (
    delivery_id UUID NOT NULL,
    country VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    house VARCHAR(255) NOT NULL,
    flat VARCHAR(255) NOT NULL,
    PRIMARY KEY (delivery_id),
    CONSTRAINT delivery_id
        FOREIGN KEY (delivery_id)
        REFERENCES delivery (id)
        ON DELETE CASCADE
);
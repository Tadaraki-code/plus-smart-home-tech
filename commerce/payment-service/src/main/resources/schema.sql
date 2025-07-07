CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID,
    payment_state VARCHAR(255),
    total_price NUMERIC(9,2),
    delivery_price NUMERIC(9,2),
    fee_total NUMERIC(9,2)
);
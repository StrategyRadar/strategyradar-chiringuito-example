-- Create orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on created_at for performance
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Create index on status for filtering
CREATE INDEX idx_orders_status ON orders(status);